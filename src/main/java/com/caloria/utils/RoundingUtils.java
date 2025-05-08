// src/main/java/com/caloria/utils/RoundingUtils.java
package com.caloria.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public final class RoundingUtils {
  private RoundingUtils() {}

  /** Redondea un double a N decimales, half–up. */
  public static double round(double v, int decimals) {
    return BigDecimal.valueOf(v)
                     .setScale(decimals, RoundingMode.HALF_UP)
                     .doubleValue();
  }

  /** Redondea un double a 1 decimal, half–up. */
  public static double oneDecimal(double v) {
    return round(v, 1);
  }

  /** Redondea un double a 0 decimales (entero), half–up. */
  public static double toInteger(double v) {
    return round(v, 0);
  }

  /** Función auxiliar para redondeo a N decimales en streams o lambdas. */
  public static Function<Double, Double> roundFn(int decimals) {
    return v -> round(v, decimals);
  }

  /** Función auxiliar para 1 decimal. */
  public static Function<Double, Double> oneDecimalFn() {
    return roundFn(1);
  }

  /** Función auxiliar para 0 decimales. */
  public static Function<Double, Double> integerFn() {
    return roundFn(0);
  }
}