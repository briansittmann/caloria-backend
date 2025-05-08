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

@Service
@Slf4j
public class JwtService {

    /** Clave de al menos 32 caracteres (256 bits) en application.properties */
    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    /** Se ejecuta una sola vez después de inyectar la propiedad */
    @PostConstruct
    private void init() {
        // Crea la clave a partir del texto; si no son 32+ bytes, JJWT lanzará excepción
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un JWT válido durante 24 h con el usuarioId en el subject y
     * un claim “roles”.
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

    /** Devuelve true si la firma y la expiración son válidas */
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

    /** Extrae el usuarioId (subject) del token ya validado */
    public String getUsuarioId(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }
}