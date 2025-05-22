package com.caloria.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


/**
 * Configuración personalizada de Jackson para la serialización y deserialización JSON.
 *
 * Esta clase configura un `ObjectMapper` global con soporte para Kotlin
 * y evita errores al serializar objetos vacíos.
 */
@Configuration
public class JacksonConfig {
	
	
    /**
     * Crea y configura el `ObjectMapper` utilizado por Spring para manejar JSON.
     *
     * - Habilita el módulo de compatibilidad con Kotlin.
     * - Evita que se lancen errores al serializar beans vacíos (FAIL_ON_EMPTY_BEANS).
     *
     * @return Objeto `ObjectMapper` listo para ser inyectado en el contexto de Spring.
     */
    @SuppressWarnings("deprecation")
	@Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new KotlinModule.Builder()
                        .nullIsSameAsDefault(true)
                        .build())
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }
}