package com.caloria.model;

public enum ObjetivoNutricional {
    CUT_LIGERO(0.90),
    CUT_MEDIO(0.80),
    CUT_AGRESIVO(0.75),
    BULK_CONSERVADOR(1.05),
    BULK_ESTANDAR(1.10),
    BULK_AGRESIVO(1.15);

    private final double factor;
    ObjetivoNutricional(double factor) { this.factor = factor; }
    public double getFactor() { return factor; }
}