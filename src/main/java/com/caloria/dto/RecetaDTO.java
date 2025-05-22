package com.caloria.dto;


import lombok.Data;
import java.util.List;

/**
 * DTO utilizado para representar una receta completa (título, ingredientes, instrucciones y macros),
 * ya sea generada por IA o guardada por el usuario.
 */
@Data
public class RecetaDTO {
  private String titulo;
  private int calorias;
  private List<IngredienteDTO> ingredientes;
  private String instrucciones;
  private MacroNutrientesDTO macroNutrientes;
}

/**
 * DTO auxiliar que representa un ingrediente individual de una receta,
 * con su nombre y cantidad en gramos.
 */
@Data
class IngredienteDTO {
  private String nombre;
  private double gramos;
}

/**
 * DTO auxiliar que representa la distribución de macronutrientes de una receta
 * en gramos: proteínas, carbohidratos y grasas.
 */
@Data
class MacroNutrientesDTO {
  private int proteinas;
  private int carbohidratos;
  private int grasas;
}