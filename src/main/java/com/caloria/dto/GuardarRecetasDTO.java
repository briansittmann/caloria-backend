package com.caloria.dto;


import lombok.Data;
import java.util.List;


/**
 * DTO que encapsula una lista de recetas que el usuario desea guardar como favoritas.
 * Se utiliza en el endpoint de guardar múltiples recetas.
 */
@Data
public class GuardarRecetasDTO {
  private List<RecetaDTO> recetas;
}