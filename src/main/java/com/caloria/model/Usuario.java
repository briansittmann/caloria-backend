package com.caloria.model;

import com.caloria.utils.RoundingUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Entidad que representa el perfil completo de un usuario,
 * incluyendo datos personales, preferencias alimenticias, historial diario
 * y metas nutricionales generadas por IA.
 *
 * El perfil se construye por etapas (básicos, actividad, objetivo, preferencias),
 * y una vez completo, se calculan y almacenan las calorías y macros objetivo.
 */
@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

 // Paso 1 – básicos
    private String email;
    private String nombre;
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String horaInicioDia;
    private boolean basicosCompletos = false;

    // Paso 2 – actividad
    private String nivelActividad;
    private boolean actividadCompleta = false;

    // Paso 3 – objetivo
    private String objetivo;
    private boolean objetivoCompleto = false;

    // Paso 4 – preferencias
    private List<String> preferencias = new ArrayList<>();
    private List<String> alergias      = new ArrayList<>();
    private boolean preferenciasCompletas = false;

    // Metas (solo cuando todo lo anterior esté completo)
    private double caloriasObjetivo;
    private Macros macrosObjetivo;
    private boolean perfilCompleto = false;

    
    // Historial de progreso
    @JsonIgnore
    private List<Dia> historialDeDias = new ArrayList<>();
 // Recetas guardadas por el usuario
    private List<String> recetas = new ArrayList<>();

    /**
     * Devuelve el día activo del usuario teniendo en cuenta la hora de inicio.
     * Si no existe, lo crea.
     *
     * La hora de inicio permite que el día nutricional comience, por ejemplo,
     * a las 04:00 para usuarios nocturnos. Esto influye en la fecha efectiva.
     *
     * @return Objeto Dia correspondiente a la jornada actual.
     */
    public Dia obtenerDiaActual() {
        LocalTime inicio = LocalTime.parse(this.horaInicioDia);
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        LocalDate fechaEfectiva = ahora.isBefore(inicio)
            ? hoy.minusDays(1)
            : hoy;

        for (Dia dia : historialDeDias) {
            if (dia.getFecha().equals(fechaEfectiva)) {
                return dia;
            }
        }

        Dia nuevo = new Dia();
        nuevo.setFecha(fechaEfectiva);
        nuevo.setHoraInicioDia(inicio);
        historialDeDias.add(nuevo);
        return nuevo;
    }


    /**
     * Suma una nueva ingesta de macronutrientes al día actual,
     * redondeando cada valor a un decimal.
     *
     * @param proteinas gramos de proteína
     * @param carbohidratos gramos de carbohidratos
     * @param grasas gramos de grasa
     * @param calorias kilocalorías
     */
    public void actualizarMacronutrientes(
            Double proteinas, Double carbohidratos, Double grasas, Double calorias) {

        // redondear cada valor antes de agregar
        double p = RoundingUtils.oneDecimal(proteinas);
        double c = RoundingUtils.oneDecimal(carbohidratos);
        double g = RoundingUtils.oneDecimal(grasas);
        double k = RoundingUtils.oneDecimal(calorias);

        Dia dia = obtenerDiaActual();
        dia.agregarMacronutrientes(p, c, g, k);
    }

    /**
     * Aplica los objetivos nutricionales del usuario redondeando a enteros y 1 decimal.
     * Se invoca una vez que el perfil está completamente configurado.
     *
     * @param calorias calorías diarias objetivo
     * @param macros macronutrientes diarios objetivo
     */
    public void aplicarMetas(double calorias, Macros macros) {
        // Calorías a entero
        this.caloriasObjetivo = (int) Math.round(calorias);

        // Macros a 1 decimal
        Macros redondeados = new Macros();
        redondeados.setProteinasG(   RoundingUtils.oneDecimal(macros.getProteinasG())   );
        redondeados.setCarbohidratosG(RoundingUtils.oneDecimal(macros.getCarbohidratosG()));
        redondeados.setGrasasG(      RoundingUtils.oneDecimal(macros.getGrasasG())      );

        this.macrosObjetivo = redondeados;
    }
}