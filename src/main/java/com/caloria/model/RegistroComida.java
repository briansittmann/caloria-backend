package com.caloria.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "comidas")
public class RegistroComida {
    @Id
    private String id;
    private String usuarioId;
    private LocalDateTime fechaIngreso;
    private String textoOriginal;
    private String fechaDiaNutricional; // formato "2025-04-03"
    private String horaInicioDia;       // "04:00"
    private List<Alimento> alimentos;
    private Macros totales;
    private boolean procesadoPorOpenAI;
    private LocalDateTime fechaProcesamiento;
}