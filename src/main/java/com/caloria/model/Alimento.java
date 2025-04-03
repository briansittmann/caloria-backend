package com.caloria.model;

import lombok.Data;

@Data
public class Alimento {
    private String nombre;
    private String cantidadEstimada;
    private double calorias;
    private double proteinasG;
    private double carbohidratosG;
    private double grasasG;
}