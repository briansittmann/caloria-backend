package com.caloria.dto;

import com.caloria.model.Macros;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data // Lombok generará automáticamente getters, setters, toString, etc.
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    private String email;

    @Min(value = 18, message = "La edad debe ser al menos 18 años")
    @Min(value = 100, message = "La edad no puede ser mayor a 100 años")
    private Integer edad;

    @NotBlank(message = "El sexo no puede estar vacío")
    private String sexo;

    @Min(value = 0, message = "La altura debe ser mayor o igual a 0")
    private Integer alturaCm;

    @Min(value = 0, message = "El peso debe ser mayor o igual a 0")
    private Integer pesoKg;

    @NotBlank(message = "El nivel de actividad no puede estar vacío")
    private String nivelActividad;

    @NotBlank(message = "El objetivo no puede estar vacío")
    @Pattern(regexp = "bajar_grasa|ganar_musculo|mantener", message = "El objetivo debe ser uno de los siguientes: bajar_grasa, ganar_musculo, mantener")
    private String objetivo;

    @Min(value = 0, message = "Las calorías objetivo deben ser un valor positivo")
    private Integer caloriasObjetivo;

    private Macros macrosObjetivo;

    @NotBlank(message = "La hora de inicio del día no puede estar vacía")
    private String horaInicioDia;

    // Campo para la contraseña
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}