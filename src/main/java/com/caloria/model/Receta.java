package com.caloria.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Entidad que representa una receta personalizada o sugerida por la IA.
 *
 * Se almacena en la colección `catalogoRecetas` y contiene información nutricional
 * y estructural para que el usuario pueda implementarla fácilmente en su dieta.
 */
@Document(collection = "catalogoRecetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receta {
	
	/** ID único generado por MongoDB */
  @Id
  private String id;

  /** Título o nombre de la receta */
  private String titulo;

  /** Calorías totales estimadas de la receta */
  private int calorias;

  /** Lista de ingredientes necesarios con sus respectivas cantidades */
  private List<Ingrediente> ingredientes;

  /** Instrucciones de preparación en texto plano */
  private String instrucciones;

  /** Macronutrientes totales de la receta (en gramos) */
  @Field("macroNutrientes")
  private MacroNutrientes macroNutrientes;

  /**
   * Clase interna que representa un ingrediente con su peso en gramos.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Ingrediente {
      private String nombre;
      private double gramos;
  }

  /**
   * Clase interna que representa los macronutrientes totales de una receta.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MacroNutrientes {
      private int proteinas;
      private int carbohidratos;
      private int grasas;
  }
}