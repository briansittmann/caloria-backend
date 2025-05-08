package com.caloria.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

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