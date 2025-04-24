package com.caloria.model;

import lombok.Data; // Lombok para generar getters, setters, etc.
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Data // Lombok anotación para generar getters, setters, toString, equals, hashCode
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String nombre;
    private String email;
    private String password;  // Este campo ahora almacenará la contraseña en texto cifrado
    private String role;  // Por ejemplo, "usuario", "admin", etc.
    private Integer edad;
    private String sexo;
    private Integer alturaCm;
    private Integer pesoKg;
    private String nivelActividad;
    private String objetivo;  // perder_grasa, mantener, ganar_musculo
    private Integer caloriasObjetivo;
    private Macros macrosObjetivo;
    private String horaInicioDia; // "04:00"
    
    private List<Dia> historialDeDias;  // Lista que almacena el historial de días del usuario

    // Método para obtener el día actual y comprobar si necesitamos resetearlo
    public Dia obtenerDiaActual() {
        for (Dia dia : historialDeDias) {
            if (dia.getFecha().equals(java.time.LocalDate.now())) {
                dia.resetearDia(); // Verificar si el día debe resetearse
                return dia;
            }
        }
        // Si no existe un día actual, lo creamos
        Dia nuevoDia = new Dia();
        nuevoDia.setFecha(java.time.LocalDate.now());
        nuevoDia.setHoraInicioDia(java.time.LocalTime.parse(this.horaInicioDia));  // Usamos la hora de inicio del día
        historialDeDias.add(nuevoDia);
        return nuevoDia;
    }

    // Método para actualizar los macronutrientes del día
    public void actualizarMacronutrientes(Double proteinas, Double carbohidratos, Double grasas, Double calorias) {
        Dia diaActual = obtenerDiaActual();
        diaActual.agregarMacronutrientes(proteinas, carbohidratos, grasas, calorias);
    }
}