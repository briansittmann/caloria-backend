package com.caloria.dto;

import com.caloria.model.Macros;
import lombok.Data;


/**
 * DTO que resume el estado nutricional del usuario en el día actual.
 *
 * Incluye calorías objetivo, consumidas y restantes, así como los macronutrientes
 * y el número de consejos generados por IA.
 */
@Data
public class ResumenDiaDTO {

    private String fecha;               // "2025-04-18"
    private double caloriasObjetivo;
    private double caloriasConsumidas;
    private double caloriasRestantes;

    private Macros macrosObjetivo;
    private Macros macrosConsumidos;
    private Macros macrosRestantes;
    
    private int consejosGenerados;
}

