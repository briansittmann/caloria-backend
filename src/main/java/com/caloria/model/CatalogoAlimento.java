package com.caloria.model;

import com.caloria.utils.RoundingUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa un alimento normalizado en nuestra base de datos.
 * Mantiene macros por 100 g siempre.
 */
@Data
@Document(collection = "alimentos")
public class CatalogoAlimento {
    @Id
    private String id;

    /** Nombre normalizado, p.ej. "arroz cocido" */
    private String nombre;

    /** Macronutrientes por 100 g */
    private double caloriasPor100g;
    private double proteinasPor100g;
    private double carbohidratosPor100g;
    private double grasasPor100g;

    /**
     * Construye un registro por 100 g a partir de un Alimento consumido.
     */
    public static CatalogoAlimento from(Alimento a) {
        double factor = 100.0 / a.getGramos();

        CatalogoAlimento cat = new CatalogoAlimento();
        cat.setNombre(a.getNombre());
        cat.setCaloriasPor100g(    RoundingUtils.oneDecimal(a.getCalorias() * factor));
        cat.setProteinasPor100g(   RoundingUtils.oneDecimal(a.getProteinasG() * factor));
        cat.setCarbohidratosPor100g(RoundingUtils.oneDecimal(a.getCarbohidratosG() * factor));
        cat.setGrasasPor100g(      RoundingUtils.oneDecimal(a.getGrasasG() * factor));
        return cat;
    }
}