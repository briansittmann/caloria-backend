// src/main/java/com/caloria/utils/RoundingUtils.java
package com.caloria.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Clase utilitaria que centraliza las operaciones de redondeo numérico
 * utilizadas en todo el sistema, especialmente para valores nutricionales
 * y métricas mostradas al usuario.
 *
 * Utiliza la estrategia de redondeo HALF_UP (redondeo estándar).
 * También expone funciones listas para usar en streams o lambdas.
 */
public final class RoundingUtils {

    // Constructor privado para evitar instanciación
    private RoundingUtils() {}

    /**
     * Redondea un número decimal a la cantidad de decimales indicada.
     *
     * @param v Valor original (double)
     * @param decimals Número de cifras decimales deseadas
     * @return Valor redondeado
     */
    public static double round(double v, int decimals) {
        return BigDecimal.valueOf(v)
                         .setScale(decimals, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    /**
     * Redondea un valor decimal a 1 cifra decimal.
     *
     * @param v Valor original
     * @return Valor redondeado a 1 decimal
     */
    public static double oneDecimal(double v) {
        return round(v, 1);
    }

    /**
     * Redondea un valor decimal al entero más cercano.
     *
     * @param v Valor original
     * @return Valor redondeado a 0 decimales
     */
    public static double toInteger(double v) {
        return round(v, 0);
    }

    /**
     * Devuelve una función lambda para redondear a N decimales.
     *
     * @param decimals Número de decimales deseado
     * @return Función de redondeo aplicable en streams
     */
    public static Function<Double, Double> roundFn(int decimals) {
        return v -> round(v, decimals);
    }

    /**
     * Función lambda que redondea a 1 decimal.
     *
     * @return Función reutilizable para redondeo simple
     */
    public static Function<Double, Double> oneDecimalFn() {
        return roundFn(1);
    }

    /**
     * Función lambda que redondea a 0 decimales.
     *
     * @return Función reutilizable para redondeo entero
     */
    public static Function<Double, Double> integerFn() {
        return roundFn(0);
    }
}