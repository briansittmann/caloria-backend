package com.caloria.service;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.threads.Thread;                    // No confundir con java.lang.Thread
import com.cjcrafter.openai.threads.message.*;
import com.cjcrafter.openai.threads.runs.*;
import com.caloria.dto.MacrosDTO;
import com.caloria.service.CaloriasCalculator;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor                // Inyecta los final automáticamente
public class IAService {

    private final OkHttpClient okHttpClientWithHeader; // viene del Bean configurado
    private final DiaService diaService;               // NUEVO: maneja la persistencia

    // ---------  Campos propios (inicializados en el ctor) ----------
    private final OpenAI openai;
    private final Assistant assistant;

    // Constructor manual porque tenemos que crear OpenAI con okHttp
    public IAService(OkHttpClient okHttpClientWithHeader,
                     DiaService diaService) {

        this.okHttpClientWithHeader = okHttpClientWithHeader;
        this.diaService = diaService;

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) throw new IllegalStateException("API Key no configurada");
        System.out.println("API Key cargada correctamente");

        this.openai = OpenAI.builder()
                .apiKey(apiKey)
                .client(okHttpClientWithHeader)
                .build();

        // Assistant creado en el playground
        this.assistant = openai.assistants().retrieve("asst_s4TKSL0Qi7Mep2tZrr4Lw9tZ");
    }

    /**
     * Llama al Assistant con la descripción del alimento, persiste los datos
     * en el Día actual del usuario y devuelve el JSON que envió la IA.
     *
     * @param descripcionComida descripción natural (ej. "pollo 100 g")
     * @param usuarioId         id del usuario logueado
     */
    public String analizarComida(String descripcionComida, String usuarioId) throws InterruptedException {

        // 1. -------- Crear thread + mensaje ----------
        Thread thread = openai.threads().create();

        openai.threads().messages(thread).create(
                CreateThreadMessageRequest.builder()
                        .role(ThreadUser.USER)
                        .content("RgstrAlim " + descripcionComida)
                        .build());

        // 2. -------- Ejecutar run ----------
        Run run = openai.threads().runs(thread).create(
                CreateRunRequest.builder().assistant(assistant).build());

        while (!run.getStatus().isTerminal()) {
            Thread.sleep(1_000);
            run = openai.threads().runs(thread).retrieve(run);
        }

        // 3. -------- Recoger respuesta ----------
        StringBuilder resultado = new StringBuilder();
        for (RunStep step : openai.threads().runs(thread).steps(run).list().getData()) {
            if (step.getType() != RunStep.Type.MESSAGE_CREATION) continue;

            MessageCreationDetails det = (MessageCreationDetails) step.getStepDetails();
            ThreadMessage msg = openai.threads().messages(thread)
                                       .retrieve(det.getMessageCreation().getMessageId());

            msg.getContent().stream()
               .filter(c -> c.getType() == ThreadMessageContent.Type.TEXT)
               .map(c -> ((TextContent) c).getText().getValue())
               .forEach(resultado::append);
        }

        // 4. -------- Parsear JSON y manejar errores ----------
        String respuesta = resultado.toString();
        try {
            JSONObject json = new JSONObject(respuesta);

            if (json.has("error")) return json.getString("error");

            // 5. -------- Extraer macros ----------
            double prot = json.getDouble("proteinas");
            double carb = json.getDouble("carbohidratos");
            double gras = json.getDouble("grasas");
            double kcals = CaloriasCalculator.calcularCalorias(prot, carb, gras);

            // 6. -------- Persistir en el día actual ----------
            diaService.registrarAlimento(
                    usuarioId,
                    new MacrosDTO(prot, carb, gras, kcals)   // simple DTO para pasar datos
            );

            return respuesta;

        } catch (JSONException e) {
            return "Error procesando la respuesta de la IA: " + e.getMessage();
        }
    }
}