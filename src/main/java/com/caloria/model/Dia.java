package com.caloria.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;


/**
 * Representa el resumen nutricional de un día concreto del usuario.
 *
 * Contiene la fecha efectiva, hora de inicio del día,
 * los nutrientes consumidos, el total de calorías ingeridas
 * y un contador de consejos solicitados a la IA.
 *
 * Es utilizado por la clase {@link Usuario} para construir su historial diario.
 */
@Data
public class Dia {
	
	 /** Fecha efectiva del día, ajustada según la hora de inicio personalizada del usuario */
    private LocalDate fecha;  
    
    /** Hora a partir de la cual comienza el nuevo día nutricional */
    private LocalTime horaInicioDia;  
    
    /** Total de macronutrientes consumidos ese día (g) */
    private Double proteinasConsumidas = 0.0;
    private Double carbohidratosConsumidos = 0.0;
    private Double grasasConsumidas = 0.0;
    private Double caloriasConsumidas = 0.0;  
    
    /** Número de consejos nutricionales generados por la IA para este día */
    private int consejosGenerados = 0;

    /**
     * Incrementa los valores nutricionales con los proporcionados.
     *
     * @param proteinas gramos de proteína a sumar
     * @param carbohidratos gramos de carbohidrato a sumar
     * @param grasas gramos de grasa a sumar
     * @param calorias kilocalorías a sumar
     */
    public void agregarMacronutrientes(Double proteinas, Double carbohidratos, Double grasas, Double calorias) {
        this.proteinasConsumidas += proteinas;
        this.carbohidratosConsumidos += carbohidratos;
        this.grasasConsumidas += grasas;
        this.caloriasConsumidas += calorias;  // Sumamos las calorías
    }
    
    /**
     * Verifica si la hora actual ha sobrepasado la hora de inicio del día,
     * lo que indicaría que ha comenzado un nuevo ciclo nutricional.
     *
     * @return true si es un nuevo día, false en caso contrario
     */
    public boolean esNuevoDia() {
        LocalTime currentTime = LocalTime.now();
        return currentTime.isAfter(horaInicioDia);
    }

    /**
     * Resetea los valores nutricionales del día y actualiza la fecha si corresponde.
     * Este método debe llamarse al detectar un cambio de día efectivo.
     */    public void resetearDia() {
        if (esNuevoDia()) {
            // Si es un nuevo día, reiniciamos los valores
            this.proteinasConsumidas = 0.0;
            this.carbohidratosConsumidos = 0.0;
            this.grasasConsumidas = 0.0;
            this.caloriasConsumidas = 0.0;
            this.fecha = LocalDate.now();  // Actualizamos la fecha
        }
    }
    

     /**
      * Aumenta en 1 el contador de consejos solicitados para este día.
      */
    public void incrementarConsejosGenerados() {
        this.consejosGenerados++;
    }
}