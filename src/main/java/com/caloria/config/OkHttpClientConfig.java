package com.caloria.config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuración personalizada de OkHttpClient para llamadas a la API de OpenAI Assistants (v2).
 *
 * Este cliente:
 * - Agrega el header `OpenAI-Beta: assistants=v2` requerido por la API.
 * - Intercepta respuestas JSON para asegurar la presencia del campo `file_ids` en determinadas rutas.
 *
 * Es utilizado por el servicio `IAService` para interactuar con los asistentes de OpenAI.
 */
@Configuration
public class OkHttpClientConfig {
	
	
    /**
     * Crea un cliente OkHttp con un interceptor que modifica tanto la petición
     * como la respuesta para adaptarse a las restricciones de la API de OpenAI.
     *
     * - Añade un header necesario para habilitar la versión v2 de Assistants.
     * - Si la respuesta JSON de ciertos endpoints no contiene `file_ids`, lo inyecta.
     *
     * @return Cliente OkHttp personalizado para IA
     */
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