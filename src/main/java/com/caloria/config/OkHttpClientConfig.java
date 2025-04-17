package com.caloria.config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpClientConfig {

    @Bean
    public OkHttpClient okHttpClientWithHeader() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    // Agregar el header requerido para usar la versión v2 de Assistants
                    Request original = chain.request();
                    Request requestWithHeader = original.newBuilder()
                            .header("OpenAI-Beta", "assistants=v2")
                            .build();

                    Response response = chain.proceed(requestWithHeader);
                    String url = requestWithHeader.url().toString();

                    // Si la URL involucra endpoints de assistants o threads (por ejemplo, mensajes),
                    // aplicamos la modificación para inyectar el campo "file_ids" si falta.
                    if (url.contains("/assistants/") || url.contains("/threads/messages") || url.contains("/threads/")) {
                        if (response.body() != null && response.body().contentType() != null &&
                                response.body().contentType().toString().contains("application/json")) {
                            // Leer el cuerpo de la respuesta como cadena (esto agota el body)
                            String bodyString = response.body().string();
                            // Si el JSON no contiene el campo "file_ids", lo inyectamos
                            if (!bodyString.contains("\"file_ids\"")) {
                                // Se asume que el JSON empieza con "{" y se inserta "file_ids":[]
                                bodyString = bodyString.replaceFirst("\\{", "\\{\"file_ids\":[],");
                            }
                            // Crear un nuevo ResponseBody con el contenido modificado
                            ResponseBody newBody = ResponseBody.create(bodyString, response.body().contentType());
                            return response.newBuilder().body(newBody).build();
                        }
                    }
                    return response;
                })
                .build();
    }
}