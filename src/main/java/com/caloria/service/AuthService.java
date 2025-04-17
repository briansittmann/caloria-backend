package com.caloria.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class AuthService {

    @Value("${jwt.secret}")
    private String secretKeyString; // La clave secreta que has definido en application.properties

    private final UsuarioService usuarioService;

    public AuthService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public String login(String email, String password) {
        if (usuarioService.validarCredenciales(email, password)) {
            return generateJwt(email);  // Genera el JWT si las credenciales son correctas
        } else {
            throw new RuntimeException("Credenciales incorrectas");
        }
    }

    private String generateJwt(String email) {
        // Generar la clave secreta segura con Keys.secretKeyFor, que asegura que sea al menos de 256 bits
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        long expirationTime = 1000 * 60 * 60; // 1 hora de expiración

        // Crear y firmar el JWT usando la clave generada
        return Jwts.builder()
                .setSubject(email)  // El "sujeto" del JWT es el email del usuario
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Establecer la expiración
                .signWith(secretKey, SignatureAlgorithm.HS256) // Firmar con la clave secreta generada
                .compact();
    }
}