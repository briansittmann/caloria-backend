package com.caloria.service;

public class CaloriasCalculator {

    // Método para calcular las calorías a partir de los macronutrientes
    public static double calcularCalorias(double proteinas, double carbohidratos, double grasas) {
        double caloriasProteinas = proteinas * 4;
        double caloriasCarbohidratos = carbohidratos * 4;
        double caloriasGrasas = grasas * 9;

        return caloriasProteinas + caloriasCarbohidratos + caloriasGrasas;
    }
}