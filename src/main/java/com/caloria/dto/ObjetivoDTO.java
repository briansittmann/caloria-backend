package com.caloria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ObjetivoDTO {
    @NotNull
    private String objetivo;       // usa los valores de tu enum ObjetivoNutricional
}