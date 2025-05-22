package com.caloria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuración de Swagger/OpenAPI para generar documentación interactiva de la API.
 *
 * Permite acceder a una interfaz visual en `/swagger-ui/index.html`
 * donde se pueden probar endpoints y visualizar sus definiciones.
 */
@Configuration
public class SwaggerConfig {
	
	
    /**
     * Define la información principal del documento OpenAPI para Swagger.
     *
     * Incluye título, versión y descripción general de la API de CalorIA.
     *
     * @return Objeto OpenAPI con metadatos personalizados
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CalorIA API")
                .version("1.0")
                .description("Documentación de la API REST de CalorIA"));
    }
}