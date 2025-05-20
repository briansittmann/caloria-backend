// src/main/java/com/caloria/service/IAService.java
package com.caloria.service;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.threads.Thread;            // SDK de OpenAI
import com.cjcrafter.openai.threads.message.*;
import com.cjcrafter.openai.threads.runs.*;
import com.caloria.dto.AlimentoDTO;
import com.caloria.dto.MacrosDTO;
import com.caloria.model.Alimento;
import com.caloria.model.CatalogoAlimento;
import com.caloria.utils.RoundingUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
        // 1. API key de OpenAI
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("OPENAI_API_KEY no configurada");
        }

        // 2. IDs de los asistentes desde variables de entorno
        String foodAssistantId    = System.getenv("FOOD_AI");
        String recipesAssistantId = System.getenv("RECIPES_AI");
        if (foodAssistantId == null) {
            throw new IllegalStateException("FOOD_AI no configurada");
        }
        if (recipesAssistantId == null) {
            throw new IllegalStateException("RECIPES_AI no configurada");
        }

        // 3. Construir cliente OpenAI
        this.openai = OpenAI.builder()
                            .apiKey(apiKey)
                            .client(okHttpClientWithHeader)
                            .build();

        // 4. Recuperar asistentes por ID
        this.assistant = openai.assistants().retrieve(foodAssistantId);
        this.assistantRecetas = openai.assistants().retrieve(recipesAssistantId);
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
     * Analiza un listado de alimentos: normaliza los existentes,
     * llama a la IA para los faltantes en una sola petición,
     * devuelve el JSON de macros escalados y luego registra/guarda
     * en segundo plano.
     */
    public String analizarComida(List<AlimentoDTO> dtos, String usuarioId) throws InterruptedException {
        log.info("Iniciando análisis de {} items para usuario {}", dtos.size(), usuarioId);

        // -- 1) separar encontrados vs faltantes (igual que antes) --
        List<AlimentoDTO> encontrados = new ArrayList<>();
        List<AlimentoDTO> faltantes  = new ArrayList<>();
        for (AlimentoDTO dto : dtos) {
            boolean existe = catalogoService.obtenerPorNombre(dto.getNombre()).isPresent();
            if (dto.getGramos() > 0 && existe) {
                encontrados.add(dto);
            } else {
                faltantes.add(dto);
            }
        }

        JSONObject respuesta = new JSONObject();
        List<Alimento> toRegister = new ArrayList<>();
        int idx = 1;

        // -- 2) procesar los encontrados tal cual lo tenías --
        for (AlimentoDTO dto : encontrados) {
            CatalogoAlimento cat = catalogoService.obtenerPorNombre(dto.getNombre()).get();
            double factor = dto.getGramos() / 100.0;
            double prot   = RoundingUtils.oneDecimal(cat.getProteinasPor100g() * factor);
            double carb   = RoundingUtils.oneDecimal(cat.getCarbohidratosPor100g() * factor);
            double gras   = RoundingUtils.oneDecimal(cat.getGrasasPor100g()        * factor);

            respuesta.put("alimento_" + idx, new JSONObject()
                    .put("proteinas", prot)
                    .put("carbohidratos", carb)
                    .put("grasas",     gras)
            );

            Alimento raw = new Alimento();
            raw.setNombre(cat.getNombre());
            raw.setGramos((int) Math.round(dto.getGramos()));
            raw.setProteinasG(prot);
            raw.setCarbohidratosG(carb);
            raw.setGrasasG(gras);
            raw.setCalorias(CaloriasCalculator.calcularCalorias(prot, carb, gras));
            toRegister.add(raw);

            idx++;
        }

        // -- 3) si hay faltantes, enviarlos a la IA --
        if (!faltantes.isEmpty()) {
            StringBuilder prompt = new StringBuilder("RgstrAlim");
            for (AlimentoDTO dto : faltantes) {
                prompt.append(" ")
                      .append(dto.getNombre())
                      .append(" ")
                      .append((int) Math.round(dto.getGramos()));
            }
            log.info("Enviando a IA: {}", prompt);

            // crea hilo y run, igual que antes...
            Thread thread = openai.threads().create();
            openai.threads().messages(thread).create(
                CreateThreadMessageRequest.builder()
                    .role(ThreadUser.USER)
                    .content(prompt.toString())
                    .build()
            );
            Run run = openai.threads().runs(thread)
                              .create(CreateRunRequest.builder()
                                  .assistant(assistant)
                                  .build());
            while (!run.getStatus().isTerminal()) {
                java.lang.Thread.sleep(1000);
                run = openai.threads().runs(thread).retrieve(run);
            }

            // recuperamos el texto completo de la IA
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

            JSONObject iaJson = new JSONObject(sb.toString());

            // **aquí** comprobamos si viene error en lugar de objetos de alimentos
            if (iaJson.has("error")) {
                String mensaje = iaJson.getString("error");
                log.warn("La IA devolvió un error: {}", mensaje);
                // devolvemos justo ese JSON de error y salimos
                return iaJson.toString();
            }

            // -- 4) integrar los pares proteína/carbos/grasas de la IA --
            for (String key : iaJson.keySet()) {
                JSONObject d = iaJson.getJSONObject(key);
                double prot = d.getDouble("proteinas");
                double carb = d.getDouble("carbohidratos");
                double gras = d.getDouble("grasas");
                int grs     = d.has("gramos") ? d.getInt("gramos") : 0;

                respuesta.put("alimento_" + idx, new JSONObject()
                        .put("proteinas", prot)
                        .put("carbohidratos", carb)
                        .put("grasas",     gras)
                        .put("gramos",     grs)
                );

                Alimento raw = new Alimento();
                raw.setNombre(key);
                raw.setGramos(grs);
                raw.setProteinasG(prot);
                raw.setCarbohidratosG(carb);
                raw.setGrasasG(gras);
                raw.setCalorias(CaloriasCalculator.calcularCalorias(prot, carb, gras));
                toRegister.add(raw);

                idx++;
            }
        }

        // -- 5) devolvemos el JSON combinado al cliente ya mismo --
        String salida = respuesta.toString();

        // -- 6) persistimos en background --
        CompletableFuture.runAsync(() -> {
            for (Alimento a : toRegister) {
                catalogoService.guardarSiNoExiste(a);
                diaService.registrarAlimento(usuarioId,
                    new MacrosDTO(a.getProteinasG(), a.getCarbohidratosG(), a.getGrasasG(), a.getCalorias()));
            }
            log.info("Persistencia completada ({} items)", toRegister.size());
        });

        return salida;
    }
}
