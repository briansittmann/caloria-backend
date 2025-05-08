package com.caloria.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BasicosDTO {
    @NotBlank        private String nombre;
    @NotNull @Min(18) @Max(100) private Integer edad;
    @NotBlank        private String sexo;          // “M” o “F”
    @NotNull @Min(0)             private Integer pesoKg;
    @NotNull @Min(0)             private Integer alturaCm;
    @NotBlank        private String horaInicioDia; // “HH:mm”
}