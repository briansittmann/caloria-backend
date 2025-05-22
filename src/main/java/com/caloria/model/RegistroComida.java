package com.caloria.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa una entrada de comida registrada por el usuario,
 * ya sea manualmente o mediante análisis automático con IA.
 *
 * Guarda información detallada sobre los alimentos detectados, su aporte nutricional
 * y la fecha efectiva del día nutricional en que fue registrada.
 */
@Data
@Document(collection = "comidas")
public class RegistroComida {

    /** ID único del registro (generado por MongoDB) */
    @Id
    private String id;

    /** ID del usuario al que pertenece esta entrada */
    private String usuarioId;

    /** Fecha y hora exacta en que el usuario ingresó la comida */
    private LocalDateTime fechaIngreso;

    /** Texto original ingresado (puede provenir de input libre) */
    private String textoOriginal;

    /** Fecha efectiva del día nutricional al que corresponde esta comida (formato: yyyy-MM-dd) */
    private String fechaDiaNutricional;

    /** Hora de inicio del día nutricional configurada por el usuario (ej: "04:00") */
    private String horaInicioDia;

    /** Lista de alimentos individuales identificados en esta comida */
    private List<Alimento> alimentos;

    /** Totales nutricionales agregados de todos los alimentos */
    private Macros totales;

    /** Flag que indica si la comida fue procesada con ayuda de OpenAI */
    private boolean procesadoPorOpenAI;

    /** Fecha y hora en que fue procesada por IA (si corresponde) */
    private LocalDateTime fechaProcesamiento;
}