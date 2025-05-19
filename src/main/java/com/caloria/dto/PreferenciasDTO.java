package com.caloria.dto;


import lombok.Data;

import java.util.List;

@Data
public class PreferenciasDTO {

  private List<String> preferencias;
  private List<String> alergias;
  // getters y setters
}