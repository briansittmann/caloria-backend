package com.caloria.dto;

import java.util.List;

import com.caloria.model.Macros;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PerfilUsuarioDTO {
    @NotBlank private String nombre;
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String nivelActividad;
    private String objetivo;
    private Integer caloriasObjetivo;
    private Macros macrosObjetivo;
    private String horaInicioDia;
    private List<String> preferencias;
    private List<String> alergias;
}