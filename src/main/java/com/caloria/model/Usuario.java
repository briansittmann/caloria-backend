package com.caloria.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;
    private String nombre;
    private String email;
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String nivelActividad;
    private String objetivo; // perder_grasa, mantener, ganar_musculo
    private Integer caloriasObjetivo;
    private Macros macrosObjetivo;
    private String horaInicioDia; // "04:00"
}