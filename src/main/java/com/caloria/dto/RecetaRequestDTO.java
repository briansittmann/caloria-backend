package com.caloria.dto;


import lombok.Data;
import jakarta.validation.constraints.*;


/**
 * DTO utilizado para solicitar la generación de recetas personalizadas por IA,
 * indicando cuántas comidas se desean.
 */
@Data
public class RecetaRequestDTO {
 @NotNull @Min(1) @Max(4)
 private Integer numComidas;
}