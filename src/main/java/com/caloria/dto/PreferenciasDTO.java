package com.caloria.dto;


import lombok.Data;

import java.util.List;


/**
 * DTO que contiene las preferencias alimenticias y alergias del usuario.
 * Representa el paso 4 del onboarding.
 */
@Data
public class PreferenciasDTO {

  private List<String> preferencias;
  private List<String> alergias;

}