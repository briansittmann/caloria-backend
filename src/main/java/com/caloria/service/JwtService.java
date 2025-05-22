package com.caloria.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;


/**
 * Servicio encargado de la generación, validación y parsing de tokens JWT
 * utilizados para autenticar usuarios dentro de la aplicación.
 *
 * Este servicio usa el estándar HMAC-SHA256 y una clave secreta definida
 * en el archivo de configuración (`application.properties`) bajo la propiedad `jwt.secret`.
 *
 * Los tokens incluyen como `subject` el ID del usuario y un `claim` adicional llamado `roles`.
 */
@Service
@Slf4j
public class JwtService {

    /** Clave de al menos 32 caracteres (256 bits) en application.properties */
    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    /**
     * Inicializa la clave secreta a partir del valor configurado en la propiedad `jwt.secret`.
     * 
     * Este método se ejecuta automáticamente tras la inyección del valor `secret`.
     * Utiliza codificación UTF-8 y espera una longitud mínima de 32 bytes.
     */
    @PostConstruct
    private void init() {
        // Crea la clave a partir del texto; si no son 32+ bytes, JJWT lanzará excepción
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT con una duración de 24 horas, utilizando el algoritmo HS256.
     * El token contiene como subject el `usuarioId` y un `claim` adicional con su rol.
     *
     * @param usuarioId ID del usuario autenticado
     * @param role Rol del usuario (por ejemplo, "USER", "ADMIN")
     * @return Token JWT generado y firmado
     */
    public String generateToken(String usuarioId, String role) {

        Date now  = new Date();
        Date exp  = new Date(now.getTime() + 1000L * 60 * 60 * 24); // 24 h

        return Jwts.builder()
                .setSubject(usuarioId)
                .claim("roles", List.of(role))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica si un token es válido tanto en firma como en fecha de expiración.
     *
     * @param token Token JWT a validar
     * @return true si el token es válido; false si es inválido o ha expirado
     */
    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);   // lanza excepción si está mal
            return true;
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el ID de usuario (campo `subject`) de un token ya validado.
     *
     * @param token Token JWT previamente validado
     * @return ID del usuario autenticado
     */
    public String getUsuarioId(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }
}