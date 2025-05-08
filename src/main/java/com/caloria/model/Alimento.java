package com.caloria.model;

import lombok.Data;

/**
 * Modelo de un alimento consumido, embebido en Dia.
 * No se persiste por sí solo.
 */
@Data
public class Alimento {
    private String nombre;
    private int gramos;              // peso real consumido
    private double calorias;
    private double proteinasG;
    private double carbohidratosG;
    private double grasasG;
    
    /**
     * Conveniencia para crear el registro normalizado “por 100 g”.
     * (internamente usa CatalogoAlimento.from(this))
     */
    public CatalogoAlimento toCatalogo() {
        return CatalogoAlimento.from(this);
    }
    
}