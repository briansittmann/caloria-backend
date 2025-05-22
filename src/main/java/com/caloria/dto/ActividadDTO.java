package com.caloria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO utilizado para registrar o actualizar el nivel de actividad física del usuario.
 * Representa el paso 2 del proceso de configuración del perfil.
 */
@Data
public class ActividadDTO {
    @NotNull
    private String nivelActividad; 
}