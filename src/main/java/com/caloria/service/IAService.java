// src/main/java/com/caloria/service/IAService.java
package com.caloria.service;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.threads.Thread;            // SDK de OpenAI
import com.cjcrafter.openai.threads.message.*;
import com.cjcrafter.openai.threads.runs.*;
import com.caloria.dto.MacrosDTO;
import com.caloria.model.Alimento;
import com.caloria.service.CatalogoAlimentoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IAService {
	
    private final OkHttpClient okHttpClientWithHeader;
    private final DiaService diaService;
    private final CatalogoAlimentoService catalogoService;
    private OpenAI openai;
    private Assistant assistant;
    private Assistant assistantRecetas;

    @PostConstruct
    private void init() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("OPENAI_API_KEY no configurada");
        }
        this.openai = OpenAI.builder()
                            .apiKey(apiKey)
                            .client(okHttpClientWithHeader)
                            .build();
        this.assistant = openai.assistants().retrieve("asst_s4TKSL0Qi7Mep2tZrr4Lw9tZ");
        
        // asistente para generar recetas
        this.assistantRecetas = openai.assistants()
                                      .retrieve("asst_ObZcapHILpvrxu3PnEJUBgla");
    }
    /**
     * Genera recetas basadas en preferencias, alergias, macros restantes
     * y número de comidas deseadas.
     *
     * @param preferencias lista de preferencias del usuario
     * @param alergias     lista de alergias del usuario
     * @param macrosRest   DTO con calorías y macros restantes
     * @param numComidas   número de recetas a generar (1–4)
     */
    public String generarRecetas(
        List<String> preferencias,
        List<String> alergias,
        MacrosDTO macrosRest,
        int numComidas
    ) throws InterruptedException {
        // 1) Construcción del JSON de entrada
        JSONObject payload = new JSONObject();
        payload.put("preferencias", preferencias);
        payload.put("alergias", alergias);

        JSONObject m = new JSONObject();
        m.put("caloriasRestantes",   macrosRest.getCalorias());
        m.put("proteinasRestantes",  macrosRest.getProteinasG());
        m.put("carbohidratosRestantes", macrosRest.getCarbohidratosG());
        m.put("grasasRestantes",     macrosRest.getGrasasG());
        payload.put("macrosRestantes", m);

        payload.put("numComidas", numComidas);

        // 2) Envío al asistente de recetas
        Thread thread = openai.threads().create();
        openai.threads().messages(thread).create(
            CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content(payload.toString())
                .build()
        );

        // 3) Ejecutar el run con el assistantRecetas
        Run run = openai.threads().runs(thread)
                          .create(CreateRunRequest.builder()
                              .assistant(assistantRecetas)
                              .build());
        while (!run.getStatus().isTerminal()) {
        	java.lang.Thread.sleep(1000);
            run = openai.threads().runs(thread).retrieve(run);
        }

        // 4) Concatenar respuesta
        StringBuilder sb = new StringBuilder();
        for (RunStep step : openai.threads().runs(thread).steps(run).list().getData()) {
            if (step.getType() != RunStep.Type.MESSAGE_CREATION) continue;
            MessageCreationDetails det = (MessageCreationDetails) step.getStepDetails();
            ThreadMessage msg = openai.threads().messages(thread)
                                       .retrieve(det.getMessageCreation().getMessageId());
            msg.getContent().stream()
               .filter(c -> c.getType() == ThreadMessageContent.Type.TEXT)
               .map(c -> ((TextContent) c).getText().getValue())
               .forEach(sb::append);
        }
        
        String respuesta = sb.toString();
        System.out.println("=== [IAService.generarRecetas] JSON recibido de la IA ===");
        System.out.println(respuesta);
        System.out.println("========================================================");

        // 5) Devolver el JSON puro que envía la IA
        return respuesta;
    }
    /**
     * @param nombre    nombre del alimento (p.ej. "Pollo cocido")
     * @param gramos    gramos consumidos (p.ej. 90.5)
     * @param usuarioId extraído del JWT
     */
    public String analizarComida(String nombre, double gramos, String usuarioId) throws InterruptedException {
        String descripcion = nombre + " " + (int) Math.round(gramos);

        Thread thread = openai.threads().create();
        openai.threads().messages(thread).create(
            CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content("RgstrAlim " + descripcion)
                .build()
        );

        Run run = openai.threads().runs(thread)
                          .create(CreateRunRequest.builder().assistant(assistant).build());
        while (!run.getStatus().isTerminal()) {
            java.lang.Thread.sleep(1000);
            run = openai.threads().runs(thread).retrieve(run);
        }

        StringBuilder sb = new StringBuilder();
        for (RunStep step : openai.threads().runs(thread).steps(run).list().getData()) {
            if (step.getType() != RunStep.Type.MESSAGE_CREATION) continue;
            MessageCreationDetails det = (MessageCreationDetails) step.getStepDetails();
            ThreadMessage msg = openai.threads().messages(thread)
                                       .retrieve(det.getMessageCreation().getMessageId());
            msg.getContent().stream()
               .filter(c -> c.getType() == ThreadMessageContent.Type.TEXT)
               .map(c -> ((TextContent) c).getText().getValue())
               .forEach(sb::append);
        }

        String respuesta = sb.toString();
        System.out.println(">>> Respuesta cruda de la IA: " + respuesta);

        try {
            JSONObject json = new JSONObject(respuesta);
            if (json.has("error")) return json.getString("error");

            JSONObject data;
            String key;
            if (json.has("proteinas")) {
                data = json;
                key  = nombre;
            } else {
                key  = json.keys().next();
                data = json.getJSONObject(key);
            }

            double prot  = data.getDouble("proteinas");
            double carb  = data.getDouble("carbohidratos");
            double gras  = data.getDouble("grasas");
            double kcals = CaloriasCalculator.calcularCalorias(prot, carb, gras);

            Alimento raw = new Alimento();
            raw.setNombre(key);
            raw.setGramos((int) Math.round(gramos));
            raw.setProteinasG(prot);
            raw.setCarbohidratosG(carb);
            raw.setGrasasG(gras);
            raw.setCalorias(kcals);
            catalogoService.guardarSiNoExiste(raw);

            diaService.registrarAlimento(
                usuarioId,
                new MacrosDTO(prot, carb, gras, kcals)
            );

            return respuesta;

        } catch (JSONException e) {
            return "Error procesando la respuesta de la IA: " + e.getMessage();
        }
    }
}
