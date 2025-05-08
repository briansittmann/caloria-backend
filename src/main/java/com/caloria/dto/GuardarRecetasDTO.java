package com.caloria.dto;


import lombok.Data;
import java.util.List;

@Data
public class GuardarRecetasDTO {
  private List<RecetaDTO> recetas;
}