package com.caloria.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import com.caloria.model.Macros;

@Data
public class UsuarioRequestDTO {
	@NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    private String email;

    @Min(value = 18, message = "La edad debe ser al menos 18 años")
    private Integer edad;

    @NotBlank(message = "El sexo no puede estar vacío")
    private String sexo;

    @Min(value = 0, message = "La altura debe ser mayor o igual a 0")
    private Integer alturaCm;

    @Min(value = 0, message = "El peso debe ser mayor o igual a 0")
    private Integer pesoKg;

    private String nivelActividad;
    private String objetivo;
    private Integer caloriasObjetivo;
    private Macros macrosObjetivo;
    private String horaInicioDia;
}