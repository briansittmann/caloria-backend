package com.caloria.service;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.threads.Thread;            
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


/**
 * Servicio encargado de la interacción con los asistentes de inteligencia artificial
 * para el análisis nutricional de alimentos y la generación de recetas personalizadas.
 *
 * Este servicio se comunica con OpenAI a través del SDK de CJCrafter para:
 * <ul>
 *   <li>Analizar alimentos que no están en el catálogo local</li>
 *   <li>Calcular automáticamente sus macronutrientes</li>
 *   <li>Persistir los datos en la base de datos</li>
 *   <li>Generar recetas nutricionales según preferencias, restricciones y objetivos</li>
 * </ul>
 *
 * Utiliza los asistentes configurados en OpenAI Playground para responder
 * a prompts específicos definidos por el desarrollador.
 *
 * @see com.caloria.dto.AlimentoDTO
 * @see com.caloria.model.Alimento
 * @see com.caloria.utils.RoundingUtils
 * @see com.cjcrafter.openai.OpenAI
 */
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
    
    
    /**
     * Inicializa el cliente OpenAI y recupera las instancias de los asistentes
     * configurados para análisis de alimentos y generación de recetas.
     *
     * Este método se ejecuta automáticamente tras la construcción del bean.
     *
     * @throws IllegalStateException si no se encuentran las variables de entorno necesarias
     */
    @PostConstruct
    private void init() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("OPENAI_API_KEY no configurada");
        }

        String foodAssistantId    = System.getenv("FOOD_AI");
        String recipesAssistantId = System.getenv("RECIPES_AI");
        if (foodAssistantId == null) {
            throw new IllegalStateException("FOOD_AI no configurada");
        }
        if (recipesAssistantId == null) {
            throw new IllegalStateException("RECIPES_AI no configurada");
        }

        this.openai = OpenAI.builder()
                            .apiKey(apiKey)
                            .client(okHttpClientWithHeader)
                            .build();

        this.assistant = openai.assistants().retrieve(foodAssistantId);
        this.assistantRecetas = openai.assistants().retrieve(recipesAssistantId);
    }
    
    
    /**
     * Genera un conjunto de recetas personalizadas utilizando un asistente de IA,
     * basado en los macronutrientes restantes del usuario, sus preferencias y alergias.
     *
     * La IA responde con un JSON con un listado de recetas distribuidas en el número
     * de comidas indicado, intentando respetar los objetivos nutricionales.
     *
     * @param preferencias Lista de alimentos o estilos alimentarios preferidos (puede ser vacía)
     * @param alergias Lista de ingredientes a evitar estrictamente
     * @param macrosRest Objeto que representa los macronutrientes restantes del usuario
     * @param numComidas Número de recetas que se desean generar
     * @return Cadena JSON con las recetas generadas por la IA
     * @throws InterruptedException si se interrumpe la espera de respuesta del asistente
     */
    public String generarRecetas(
        List<String> preferencias,
        List<String> alergias,
        MacrosDTO macrosRest,
        int numComidas
    ) throws InterruptedException {
    	
    	// Construcción del payload en formato JSON con datos del usuario
        JSONObject payload = new JSONObject();
        payload.put("preferencias", preferencias);
        payload.put("alergias", alergias);
        
        // Inserta los macronutrientes restantes en el objeto de macros
        JSONObject m = new JSONObject();
        m.put("caloriasRestantes",   macrosRest.getCalorias());
        m.put("proteinasRestantes",  macrosRest.getProteinasG());
        m.put("carbohidratosRestantes", macrosRest.getCarbohidratosG());
        m.put("grasasRestantes",     macrosRest.getGrasasG());
        payload.put("macrosRestantes", m);
        
        // Número de recetas a generar
        payload.put("numComidas", numComidas);
        
        
        // Crea un nuevo hilo de conversación con el asistente
        Thread thread = openai.threads().create();
        openai.threads().messages(thread).create(
            CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content(payload.toString()) // Envía el JSON como texto al asistente
                .build()
        );
        // Ejecuta la petición al asistente configurado para recetas
        Run run = openai.threads().runs(thread)
                          .create(CreateRunRequest.builder()
                              .assistant(assistantRecetas)
                              .build());
        
        // Espera bloqueante hasta que la IA complete la ejecución
        while (!run.getStatus().isTerminal()) {
            java.lang.Thread.sleep(1000);
            run = openai.threads().runs(thread).retrieve(run);
        }
        
        // Recoge las respuestas del asistente paso a paso
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
        
        // Logging y devolución de la respuesta
        String respuesta = sb.toString();
        log.info("=== [IAService.generarRecetas] JSON recibido de la IA ===");
        log.info(respuesta);
        log.info("========================================================");

        return respuesta;
    }
    
    /**
     * Analiza una lista de alimentos ingresados por el usuario y calcula sus macronutrientes.
     * 
     * Si el alimento ya existe en el catálogo local, se calcula directamente su aporte.
     * En caso contrario, se consulta al asistente de IA, que responde con valores estimados.
     * Todos los alimentos procesados se registran en la base de datos si no existían,
     * y se asignan al día correspondiente del usuario.
     *
     * @param dtos Lista de alimentos a analizar, incluyendo su nombre y gramos
     * @param usuarioId Identificador del usuario que solicita el análisis
     * @return Cadena JSON con los valores nutricionales de cada alimento
     * @throws InterruptedException si se interrumpe el hilo de espera de respuesta
     */
    public String analizarComida(List<AlimentoDTO> dtos, String usuarioId) throws InterruptedException {
        log.info("Iniciando análisis de {} items para usuario {}", dtos.size(), usuarioId);
        
        // Clasifica alimentos en encontrados (en catálogo) y faltantes (requieren IA)
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
        
        // Procesa los alimentos encontrados usando el catálogo
        for (AlimentoDTO dto : encontrados) {
            CatalogoAlimento cat = catalogoService.obtenerPorNombre(dto.getNombre()).get();
            double factor = dto.getGramos() / 100.0;
            double prot   = RoundingUtils.round(cat.getProteinasPor100g() * factor, 1);
            double carb   = RoundingUtils.round(cat.getCarbohidratosPor100g() * factor, 1);
            double gras   = RoundingUtils.round(cat.getGrasasPor100g()        * factor, 1);

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
        // Si hay alimentos no encontrados, se consulta a la IA
        if (!faltantes.isEmpty()) {
            StringBuilder prompt = new StringBuilder("RgstrAlim");
            for (AlimentoDTO dto : faltantes) {
                prompt.append(" ")
                      .append(dto.getNombre())
                      .append(" ")
                      .append((int) Math.round(dto.getGramos()));
            }
            log.info("Enviando a IA: {}", prompt);

            Thread thread = openai.threads().create();
            openai.threads().messages(thread).create(
                CreateThreadMessageRequest.builder()
                    .role(ThreadUser.USER)
                    .content(prompt.toString())
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
            
            // Verifica si hubo error y parsea respuesta JSON con nutrientes
            JSONObject iaJson = new JSONObject(sb.toString());
            if (iaJson.has("error")) {
                String mensaje = iaJson.getString("error");
                log.warn("La IA devolvió un error: {}", mensaje);
                return iaJson.toString();
            }
            
         // Transforma cada entrada en un objeto persistible
            for (String key : iaJson.keySet()) {
                JSONObject d = iaJson.getJSONObject(key);
                double prot = RoundingUtils.round(d.getDouble("proteinas"), 1);
                double carb = RoundingUtils.round(d.getDouble("carbohidratos"), 1);
                double gras = RoundingUtils.round(d.getDouble("grasas"), 1);
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
        // Almacena los alimentos y registra los macros en segundo plano
        String salida = respuesta.toString();
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
