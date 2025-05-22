package com.caloria.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Configuración principal de seguridad para la API REST de CalorIA.
 *
 * Define una arquitectura sin sesiones (stateless), basada en autenticación con JWT.
 * Aplica un filtro personalizado para validar tokens en cada petición.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    
    /**
     * Codificador de contraseñas basado en BCrypt.
     * Utilizado para almacenar y verificar contraseñas en la base de datos.
     *
     * @return PasswordEncoder con cifrado fuerte
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    
    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     *
     * - Desactiva CSRF (no necesario para APIs REST).
     * - Establece política sin sesiones (stateless).
     * - Permite libre acceso solo a rutas `/auth/**` y documentación Swagger.
     * - Requiere autenticación JWT para todas las demás rutas.
     * - Inserta el filtro `JwtAuthenticationFilter` antes del procesamiento estándar de login.
     *
     * @param http Objeto de configuración HTTP
     * @return Cadena de filtros de seguridad configurada
     * @throws Exception Si hay error en la construcción de la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // no usamos sesión ni CSRF
          .csrf(csrf -> csrf.disable())
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

          // abrimos solo /auth/**  y documentos de Swagger
          .authorizeHttpRequests(auth ->
              auth
                .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
          )

          // nuestro filtro JWT antes de procesar credenciales
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}