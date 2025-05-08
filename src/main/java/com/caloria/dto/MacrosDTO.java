package com.caloria.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para transportar los macronutrientes más las calorías
 * que calcula la IA a partir de esos macros.
 */
@Data
@NoArgsConstructor          
@AllArgsConstructor         
public class MacrosDTO {

    private double proteinasG;
    private double carbohidratosG;
    private double grasasG;
    private double calorias;
}