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

@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    private String nombre;
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String nivelActividad;     // mapeado a NivelActividad
    private String objetivo;           // mapeado a ObjetivoNutricional

    private double caloriasObjetivo;
    private Macros macrosObjetivo;

    private String horaInicioDia;      // ej. "05:00"

    private boolean perfilCompleto = false;
    
    private List<String> preferencias = new ArrayList<>();
    private List<String> alergias     = new ArrayList<>();
    @JsonIgnore
    private List<Dia> historialDeDias = new ArrayList<>();
    private List<String> recetas = new ArrayList<>();

    /** Devuelve o crea el Día actual según la hora de inicio. */
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

    /** Suma y redondea macros y calorías al día actual. */
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
     * Redondea y guarda calorías y macros objetivo, marca perfil completo.
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
        this.perfilCompleto = true;
    }
}