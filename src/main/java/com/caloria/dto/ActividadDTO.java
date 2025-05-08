package com.caloria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActividadDTO {
    @NotNull
    private String nivelActividad; 
}