package com.caloria.model;

import lombok.Data;


/**
 * Objeto de dominio que representa una distribución de macronutrientes
 * en gramos: proteínas, carbohidratos y grasas.
 *
 * Se utiliza tanto para registrar el consumo diario como para definir objetivos personalizados.
 */
@Data
public class Macros {
	
	/** Gramos de proteína */
    private double proteinasG;
    
    /** Gramos de carbohidratos */
    private double carbohidratosG;
    
    /** Gramos de grasa */
    private double grasasG;
}