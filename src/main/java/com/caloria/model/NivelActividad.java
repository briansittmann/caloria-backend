package com.caloria.model;


/**
 * Enum que representa los niveles de actividad física del usuario.
 *
 * Cada nivel se asocia con un factor PAL (Physical Activity Level),
 * utilizado para calcular el TDEE (Total Daily Energy Expenditure).
 *
 * Los valores están basados en estimaciones estándar de gasto calórico
 * según el estilo de vida y la frecuencia de ejercicio.
 */
public enum NivelActividad {
	
    MUY_BAJA(1.20),		// Estilo de vida sedentario, sin ejercicio
    
    BAJA(1.35),			// Actividad ligera ocasional (1-2 veces por semana)
    
    MODERADA(1.55),		// Actividad física moderada (3-4 veces por semana)
    
    ALTA(1.725),		// Ejercicio frecuente e intenso
    
    MUY_ALTA(1.90),		// Entrenamientos diarios muy exigentes
    
    EXTREMA(2.20);		// Deportistas de élite o trabajo físico extenuante
	
    private final double pal;
    

    /**
     * Constructor que asigna el factor PAL.
     *
     * @param pal Valor multiplicador para calcular TDEE
     */
    NivelActividad(double pal) { this.pal = pal; }
    
    
    /**
     * Devuelve el valor numérico asociado al nivel de actividad.
     *
     * @return Factor PAL correspondiente
     */
    public double getPal() { return pal; }
}