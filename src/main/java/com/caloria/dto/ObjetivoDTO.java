package com.caloria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * DTO que representa el objetivo nutricional del usuario,
 * como parte del paso 3 del onboarding (cut, mantenimiento, bulk).
 */
@Data
public class ObjetivoDTO {
    @NotNull
    private String objetivo;       // usa los valores de tu enum ObjetivoNutricional
}