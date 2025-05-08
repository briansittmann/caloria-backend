package com.caloria.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RecetaRequestDTO {
 @NotNull @Min(1) @Max(4)
 private Integer numComidas;
}