package com.caloria.dto;

/**
 * Respuesta estándar: solo devolvemos el JWT.
 */
public record LoginResponseDTO(String token) {
}