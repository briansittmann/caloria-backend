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

@Service
@RequiredArgsConstructor
public class DiaService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve un DTO con las calorías y macros restantes
     * del día actual, redondeando TODO a entero, e incluye
     * cuántos consejos ya se han generado hoy.
     */
    public ResumenDiaDTO getResumenDelDia(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        Dia dia = usuario.obtenerDiaActual();

        // Valores crudos
        double calObj  = usuario.getCaloriasObjetivo();
        double calCons = dia.getCaloriasConsumidas();

        Macros macObj = usuario.getMacrosObjetivo() != null
                        ? usuario.getMacrosObjetivo()
                        : new Macros();
        double protObj = macObj.getProteinasG();
        double carbObj = macObj.getCarbohidratosG();
        double fatObj  = macObj.getGrasasG();

        double protCons = dia.getProteinasConsumidas();
        double carbCons = dia.getCarbohidratosConsumidos();
        double fatCons  = dia.getGrasasConsumidas();

        // Función de redondeo a entero
        Function<Double, Double> rdInt = v -> (double) Math.round(v);

        // Calorías restantes
        double calRest = rdInt.apply(Math.max(0, calObj - calCons));

        // Macros consumidos redondeados
        Macros macrosConsumidos = new Macros();
        macrosConsumidos.setProteinasG(rdInt.apply(protCons));
        macrosConsumidos.setCarbohidratosG(rdInt.apply(carbCons));
        macrosConsumidos.setGrasasG(rdInt.apply(fatCons));

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

        // Construcción del DTO
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
     * Registra los macros de un alimento en el día actual del usuario.
     */
    public void registrarAlimento(String usuarioId, MacrosDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        usuario.actualizarMacronutrientes(
            dto.getProteinasG(),
            dto.getCarbohidratosG(),
            dto.getGrasasG(),
            dto.getCalorias()
        );

        usuarioRepository.save(usuario);
    }

    /**
     * Solicita un nuevo consejo de recetas. Lanza 400
     * si ya se han generado 3 hoy, y guarda el incremento.
     * Devuelve el resumen actualizado (incluye el nuevo conteo).
     */
    public ResumenDiaDTO solicitarConsejo(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        Dia dia = usuario.obtenerDiaActual();

        if (dia.getConsejosGenerados() >= 100) {
            throw new ResponseStatusException(BAD_REQUEST, "Ya has usado los 3 consejos diarios");
        }

        // Incrementa y persiste
        dia.incrementarConsejosGenerados();
        usuarioRepository.save(usuario);

        // Devuelve el resumen con el nuevo contador
        return getResumenDelDia(usuarioId);
    }
}