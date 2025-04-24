package com.caloria.dto;

import com.caloria.model.Macros;
import lombok.Data;

@Data
public class ResumenDiaDTO {

    private String fecha;               // "2025-04-18"
    private double caloriasObjetivo;
    private double caloriasConsumidas;
    private double caloriasRestantes;

    private Macros macrosObjetivo;
    private Macros macrosConsumidos;
    private Macros macrosRestantes;
}