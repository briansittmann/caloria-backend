package com.caloria.dto;


import lombok.Data;
import java.util.List;

@Data
public class RecetaDTO {
  private String titulo;
  private int calorias;
  private List<IngredienteDTO> ingredientes;
  private String instrucciones;
  private MacroNutrientesDTO macroNutrientes;
}

@Data
class IngredienteDTO {
  private String nombre;
  private double gramos;
}

@Data
class MacroNutrientesDTO {
  private int proteinas;
  private int carbohidratos;
  private int grasas;
}