package com.caloria.service;

import com.caloria.model.NivelActividad;
import com.caloria.model.ObjetivoNutricional;
import com.caloria.model.Macros;
import org.springframework.stereotype.Service;

/**
 * Abreviaturas utilizadas en esta clase:
 * 
 * - BMR: Basal Metabolic Rate (Tasa Metabólica Basal)
 * - TDEE: Total Daily Energy Expenditure (Gasto Energético Diario Total)
 * - PAL: Physical Activity Level (Nivel de Actividad Física)
 * - g: gramos
 * - kcal: kilocalorías
 */

@Service
public class MetabolismoService {

    /**
     * 1) Calcula el BMR usando la fórmula de Mifflin–St Jeor.
     * 
     * @param pesoKg Peso en kilogramos
     * @param alturaCm Altura en centímetros
     * @param edad Edad en años
     * @param sexo "F" para femenino, cualquier otro valor para masculino
     * @return BMR calculado
     */
    public double calcularBmr(double pesoKg, int alturaCm, int edad, String sexo) {
        if ("F".equalsIgnoreCase(sexo)) {
            return 10 * pesoKg + 6.25 * alturaCm - 5 * edad - 161;
        } else {
            return 10 * pesoKg + 6.25 * alturaCm - 5 * edad + 5;
        }
    }

    /**
     * 2) Calcula el TDEE multiplicando el BMR por el PAL (nivel de actividad física).
     * 
     * @param bmr Tasa metabólica basal
     * @param nivel Nivel de actividad física
     * @return TDEE calculado
     */
    public double calcularTdee(double bmr, NivelActividad nivel) {
        return bmr * nivel.getPal();
    }

    /**
     * 3) Ajusta el TDEE según el objetivo nutricional (por ejemplo, definición o volumen).
     * 
     * @param tdee Gasto energético diario total
     * @param obj Objetivo nutricional (cut = definición, bulk = volumen)
     * @return Calorías ajustadas según el objetivo
     */
    public double calcularCaloriasObjetivo(double tdee, ObjetivoNutricional obj) {
        return tdee * obj.getFactor();
    }

    /**
     * 4) Genera una distribución de macronutrientes (en gramos) a partir de las calorías objetivo.
     * 
     * Por defecto se utiliza la siguiente distribución:
     * - 30% proteínas
     * - 25% grasas
     * - 45% carbohidratos
     * 
     * @param caloriasObjetivo Calorías diarias objetivo
     * @return Objeto Macros con proteínas, grasas y carbohidratos en gramos
     */
    public Macros calcularMacrosObjetivo(double caloriasObjetivo) {
        double protKcal = caloriasObjetivo * 0.30;
        double grasaKcal = caloriasObjetivo * 0.25;
        double carbKcal = caloriasObjetivo - protKcal - grasaKcal;

        Macros m = new Macros();
        m.setProteinasG(protKcal / 4);   // 1 g proteína = 4 kcal
        m.setGrasasG(grasaKcal / 9);     // 1 g grasa = 9 kcal
        m.setCarbohidratosG(carbKcal / 4); // 1 g carbohidrato = 4 kcal
        return m;
    }
}