package com.caloria.model;

/**
 * Enum que representa los objetivos nutricionales seleccionados por el usuario.
 *
 * Cada objetivo define un factor de ajuste sobre el TDEE:
 * - Menor a 1 para pérdida de peso (cut)
 * - Igual a 1 para mantenimiento
 * - Mayor a 1 para ganancia de peso (bulk)
 */
public enum ObjetivoNutricional {

    CUT_LIGERO(0.90),       // Déficit leve (-10% por debajo del TDEE)
    
    CUT_MEDIO(0.80),        // Déficit medio (-20%)
    
    CUT_AGRESIVO(0.75),     // Déficit agresivo (-25%)
    
    MANTENER(1.00),         // Ingesta igual al TDEE
    
    BULK_CONSERVADOR(1.05), // Superávit leve (+5%)
    
    BULK_ESTANDAR(1.10),    // Superávit estándar (+10%)
    
    BULK_AGRESIVO(1.15);    // Superávit elevado (+15%)
	
	
    private final double factor;
    
    /**
     * Constructor que asigna el factor de ajuste calórico.
     *
     * @param factor Multiplicador aplicado al TDEE
     */
    ObjetivoNutricional(double factor) { this.factor = factor; }
    
    
    /**
     * Devuelve el factor asociado al objetivo.
     *
     * @return Factor de ajuste calórico
     */
    public double getFactor() { return factor; }
}