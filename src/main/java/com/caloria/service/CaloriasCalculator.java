package com.caloria.service;


/**
 * Clase utilitaria para calcular el contenido calórico de un alimento
 * a partir de sus macronutrientes.
 *
 * Utiliza los factores estándar:
 * - 1 g proteína = 4 kcal
 * - 1 g carbohidrato = 4 kcal
 * - 1 g grasa = 9 kcal
 */
public class CaloriasCalculator {


    /**
     * Calcula las calorías totales de un alimento o receta a partir de los macronutrientes.
     *
     * @param proteinas gramos de proteína
     * @param carbohidratos gramos de carbohidratos
     * @param grasas gramos de grasa
     * @return calorías estimadas en kilocalorías (kcal)
     */
    public static double calcularCalorias(double proteinas, double carbohidratos, double grasas) {
        double caloriasProteinas = proteinas * 4;
        double caloriasCarbohidratos = carbohidratos * 4;
        double caloriasGrasas = grasas * 9;

        return caloriasProteinas + caloriasCarbohidratos + caloriasGrasas;
    }
}