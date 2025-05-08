package com.caloria.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "catalogoRecetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receta {
  @Id
  private String id;

  private String titulo;
  private int calorias;

  private List<Ingrediente> ingredientes;

  private String instrucciones;

  @Field("macroNutrientes")
  private MacroNutrientes macroNutrientes;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Ingrediente {
    private String nombre;
    private double gramos;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MacroNutrientes {
    private int proteinas;
    private int carbohidratos;
    private int grasas;
  }
}