package com.caloria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que recibe las credenciales en /auth/login.
 */
public record LoginRequestDTO(
        @Email               String email,
        @NotBlank            String password) {
}