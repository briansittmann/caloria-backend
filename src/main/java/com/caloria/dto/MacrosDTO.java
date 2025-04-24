package com.caloria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MacrosDTO {
    private double proteinas;
    private double carbohidratos;
    private double grasas;
    private double calorias;
}