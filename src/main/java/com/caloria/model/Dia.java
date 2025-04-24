package com.caloria.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Dia {
    private LocalDate fecha;  // Fecha del día
    private LocalTime horaInicioDia;  // Hora de inicio del día
    private Double proteinasConsumidas = 0.0;
    private Double carbohidratosConsumidos = 0.0;
    private Double grasasConsumidas = 0.0;
    private Double caloriasConsumidas = 0.0;  // Total de calorías consumidas en ese día

    // Método para agregar los macronutrientes y calorías al día
    public void agregarMacronutrientes(Double proteinas, Double carbohidratos, Double grasas, Double calorias) {
        this.proteinasConsumidas += proteinas;
        this.carbohidratosConsumidos += carbohidratos;
        this.grasasConsumidas += grasas;
        this.caloriasConsumidas += calorias;  // Sumamos las calorías
    }

    // Método que determina si el día debe resetearse
    public boolean esNuevoDia() {
        LocalTime currentTime = LocalTime.now();
        return currentTime.isAfter(horaInicioDia);
    }

    // Método para resetear los macronutrientes y las calorías al iniciar un nuevo día
    public void resetearDia() {
        if (esNuevoDia()) {
            // Si es un nuevo día, reiniciamos los valores
            this.proteinasConsumidas = 0.0;
            this.carbohidratosConsumidos = 0.0;
            this.grasasConsumidas = 0.0;
            this.caloriasConsumidas = 0.0;
            this.fecha = LocalDate.now();  // Actualizamos la fecha
        }
    }
}