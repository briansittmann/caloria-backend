// src/main/java/com/caloria/service/DiaService.java
package com.caloria.service;

import com.caloria.dto.MacrosDTO;
import com.caloria.dto.ResumenDiaDTO;
import com.caloria.model.Dia;
import com.caloria.model.Macros;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;


/**
 * Servicio encargado de gestionar la lógica diaria del usuario,
 * incluyendo el seguimiento de consumo calórico, macronutrientes
 * y generación de consejos nutricionales.
 *
 * Este servicio actúa como intermediario entre el repositorio de usuarios
 * y las acciones que modifican el estado del día actual de cada usuario.
 *
 * Funcionalidades principales:
 * <ul>
 *   <li>Obtener resumen diario nutricional</li>
 *   <li>Registrar consumo de alimentos</li>
 *   <li>Controlar límite de consejos diarios</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DiaService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve un resumen del día actual del usuario, incluyendo:
     * calorías objetivo, consumidas y restantes, además de los
     * macronutrientes y la cantidad de consejos ya generados.
     *
     * Todos los valores se devuelven redondeados a enteros.
     *
     * @param usuarioId ID del usuario autenticado
     * @return DTO con los datos del día
     */
    public ResumenDiaDTO getResumenDelDia(String usuarioId) {
    	// Verifica existencia del usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        Dia dia = usuario.obtenerDiaActual();

        // Obtención de valores objetivo y consumidos
        double calObj  = usuario.getCaloriasObjetivo();
        double calCons = dia.getCaloriasConsumidas();
        
        
        // Obtención de objetivos de macros (pueden ser null)
        Macros macObj = usuario.getMacrosObjetivo() != null
                        ? usuario.getMacrosObjetivo()
                        : new Macros();
        
        // Valores individuales
        double protObj = macObj.getProteinasG();
        double carbObj = macObj.getCarbohidratosG();
        double fatObj  = macObj.getGrasasG();

        double protCons = dia.getProteinasConsumidas();
        double carbCons = dia.getCarbohidratosConsumidos();
        double fatCons  = dia.getGrasasConsumidas();

        // Función de redondeo a entero
        Function<Double, Double> rdInt = v -> (double) Math.round(v);

        // Calorías restantes sin permitir negativos
        double calRest = rdInt.apply(Math.max(0, calObj - calCons));

        // Macros consumidos redondeados
        Macros macrosConsumidos = new Macros();
        macrosConsumidos.setProteinasG(rdInt.apply(protCons));
        macrosConsumidos.setCarbohidratosG(rdInt.apply(carbCons));
        macrosConsumidos.setGrasasG(rdInt.apply(fatCons));
        
        // Construcción de objetos con valores redondeados
        // Macros objetivo redondeados
        Macros macrosObjetivoRedondeados = new Macros();
        macrosObjetivoRedondeados.setProteinasG(rdInt.apply(protObj));
        macrosObjetivoRedondeados.setCarbohidratosG(rdInt.apply(carbObj));
        macrosObjetivoRedondeados.setGrasasG(rdInt.apply(fatObj));

        // Macros restantes redondeados
        Macros macrosRestantes = new Macros();
        macrosRestantes.setProteinasG(rdInt.apply(Math.max(0, protObj - protCons)));
        macrosRestantes.setCarbohidratosG(rdInt.apply(Math.max(0, carbObj - carbCons)));
        macrosRestantes.setGrasasG(rdInt.apply(Math.max(0, fatObj - fatCons)));

        // Ensamblado del DTO de respuesta
        ResumenDiaDTO dto = new ResumenDiaDTO();
        dto.setFecha(LocalDate.now().toString());
        dto.setCaloriasObjetivo(Math.round(calObj));
        dto.setCaloriasConsumidas(Math.round(calCons));
        dto.setCaloriasRestantes(calRest);
        dto.setMacrosObjetivo(macrosObjetivoRedondeados);
        dto.setMacrosConsumidos(macrosConsumidos);
        dto.setMacrosRestantes(macrosRestantes);
        dto.setConsejosGenerados(dia.getConsejosGenerados());

        return dto;
    }

    /**
     * Registra el consumo de un alimento en el día actual del usuario.
     * Los valores se suman al progreso diario de macronutrientes.
     *
     * @param usuarioId ID del usuario
     * @param dto Objeto con los valores nutricionales a registrar
     */
    public void registrarAlimento(String usuarioId, MacrosDTO dto) {
    	// Verifica si el usuario existe en la base de datos
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        
        // Aplica la actualización de macros al día actual
        usuario.actualizarMacronutrientes(
            dto.getProteinasG(),
            dto.getCarbohidratosG(),
            dto.getGrasasG(),
            dto.getCalorias()
        );
        // Persiste los cambios en el repositorio
        usuarioRepository.save(usuario);
    }

    /**
     * Solicita un nuevo consejo de recetas. Lanza 400
     * si ya se han generado 3 hoy, y guarda el incremento.
     * Devuelve el resumen actualizado (incluye el nuevo conteo).
     *
     * @param usuarioId ID del usuario autenticado
     * @return DTO con los datos del día actualizado tras generar un consejo
     */
    public ResumenDiaDTO solicitarConsejo(String usuarioId) {
    	// Verifica si el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        // Accede al día actual del usuario
        Dia dia = usuario.obtenerDiaActual();
        
        // Verifica que no se haya excedido el límite diario de consejos (3)
        if (dia.getConsejosGenerados() >= 100) {
            throw new ResponseStatusException(BAD_REQUEST, "Ya has usado los 3 consejos diarios");
        }

        // Incrementa el contador de consejos usados
        dia.incrementarConsejosGenerados();
        usuarioRepository.save(usuario);

        // Devuelve el resumen del día con el contador actualizado
        return getResumenDelDia(usuarioId);
    }
}