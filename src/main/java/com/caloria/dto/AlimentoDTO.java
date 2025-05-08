package com.caloria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlimentoDTO {
    @NotBlank
    private String nombre;      // “Pollo cocido”, “Arroz integral”, etc.

    @Min(1)
    private double gramos;      // siempre en gramos, p. ej. 90.0, 37.5, 200.0
}