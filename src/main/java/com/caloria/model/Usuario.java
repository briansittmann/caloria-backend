package com.caloria.model;

import lombok.Data; // Lombok para generar getters, setters, etc.
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data // Lombok anotación para generar getters, setters, toString, equals, hashCode
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    private String nombre;
    private String email;
    private String password;  // Este campo ahora almacenará la contraseña en texto cifrado
    private String role;  // Por ejemplo, "usuario", "admin", etc.
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String nivelActividad;
    private String objetivo;  // perder_grasa, mantener, ganar_musculo
    private Integer caloriasObjetivo;
    private Macros macrosObjetivo;
    private String horaInicioDia; // "04:00"
}