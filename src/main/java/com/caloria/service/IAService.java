package com.caloria.service;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.threads.Thread; // Evita confundir con java.lang.Thread
import com.cjcrafter.openai.threads.message.CreateThreadMessageRequest;
import com.cjcrafter.openai.threads.message.ThreadUser;
import com.cjcrafter.openai.threads.message.ThreadMessage;
import com.cjcrafter.openai.threads.message.TextContent;
import com.cjcrafter.openai.threads.runs.CreateRunRequest;
import com.cjcrafter.openai.threads.runs.MessageCreationDetails;
import com.cjcrafter.openai.threads.runs.Run;
import com.cjcrafter.openai.threads.runs.RunStep;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IAService {

    private final OpenAI openai;
    private final Assistant assistant;

    @Autowired
    public IAService(OkHttpClient okHttpClientWithHeader) {
        // Leer la API Key desde las variables de entorno
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("API Key no configurada");
        }
        System.out.println("API Key cargada correctamente: " + apiKey);

        // Construir la instancia de OpenAI utilizando el builder de la clase OpenAI y estableciendo el cliente OkHttp personalizado.
        this.openai = OpenAI.builder()
                .apiKey(apiKey)
                .client(okHttpClientWithHeader)
                .build();

        // Recuperar el assistant preconfigurado (por ejemplo, creado en el Playground)
        this.assistant = openai.assistants().retrieve("asst_s4TKSL0Qi7Mep2tZrr4Lw9tZ");
    }

    public String analizarComida(String descripcionComida) throws InterruptedException {
        // Crear un nuevo thread de conversación (no usar java.lang.Thread)
        Thread thread = openai.threads().create();

        // Enviar el mensaje del usuario; en este caso, se utiliza "RgstrAlim " concatenado con la descripción de la comida.
        openai.threads().messages(thread).create(
                CreateThreadMessageRequest.builder()
                        .role(ThreadUser.USER)
                        .content("RgstrAlim " + descripcionComida)
                        .build()
        );

        // Ejecutar un run usando el assistant preconfigurado
        Run run = openai.threads().runs(thread).create(
                CreateRunRequest.builder()
                        .assistant(assistant)
                        .build()
        );

        // Busy-wait (esperar activamente) hasta que el run se complete.
        while (!run.getStatus().isTerminal()) {
            java.lang.Thread.sleep(1000);
            run = openai.threads().runs(thread).retrieve(run);
        }

        // Recopilar la respuesta del assistant recorriendo los pasos (RunStep) del run.
        StringBuilder respuestaBuilder = new StringBuilder();
        for (RunStep step : openai.threads().runs(thread).steps(run).list().getData()) {
            if (step.getType() == RunStep.Type.MESSAGE_CREATION) {
                MessageCreationDetails details = (MessageCreationDetails) step.getStepDetails();
                String messageId = details.getMessageCreation().getMessageId();
                ThreadMessage message = openai.threads().messages(thread).retrieve(messageId);
                for (com.cjcrafter.openai.threads.message.ThreadMessageContent content : message.getContent()) {
                    if (content.getType() == com.cjcrafter.openai.threads.message.ThreadMessageContent.Type.TEXT) {
                        respuestaBuilder.append(((TextContent) content).getText().getValue());
                    }
                }
            }
        }

        // Manejo de errores: intentar parsear la respuesta como JSON y verificar si hay un campo "error".
        String respuesta = respuestaBuilder.toString();
        try {
            JSONObject jsonResponse = new JSONObject(respuesta);
            if (jsonResponse.has("error")) {
                return jsonResponse.getString("error");
            }
            return respuesta;
        } catch (JSONException e) {
            return "Error procesando la respuesta de la IA. No se pudo convertir el resultado en JSON. Detalle: " + e.getMessage();
        }
    }
}