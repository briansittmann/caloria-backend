package com.caloria.model;

public enum NivelActividad {
    MUY_BAJA(1.20),
    BAJA(1.35),
    MODERADA(1.55),
    ALTA(1.725),
    MUY_ALTA(1.90),
    EXTREMA(2.20);

    private final double pal;
    NivelActividad(double pal) { this.pal = pal; }
    public double getPal() { return pal; }
}