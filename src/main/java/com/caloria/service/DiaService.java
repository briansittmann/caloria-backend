// src/main/java/com/caloria/service/DiaService.java
package com.caloria.service;

import com.caloria.dto.ResumenDiaDTO;
import com.caloria.model.Dia;
import com.caloria.model.Macros;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DiaService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve un DTO con las calorías y macros restantes del día actual.
     */
    public ResumenDiaDTO getResumenDelDia(String usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        // Día activo del usuario
        Dia dia = usuario.obtenerDiaActual();

        // Seguridad ante nulos
        double caloriasObjetivo = usuario.getCaloriasObjetivo() != null ? usuario.getCaloriasObjetivo() : 0D;
        Macros macrosObjetivo   = usuario.getMacrosObjetivo()   != null ? usuario.getMacrosObjetivo()   : new Macros();

        /*------------------------
         *  Cálculos de restantes
         *------------------------*/
        double caloriasRestantes = Math.max(0, caloriasObjetivo - dia.getCaloriasConsumidas());

        Macros macrosRestantes = new Macros();
        macrosRestantes.setProteinasG(
                Math.max(0, macrosObjetivo.getProteinasG() - dia.getProteinasConsumidas()));
        macrosRestantes.setCarbohidratosG(
                Math.max(0, macrosObjetivo.getCarbohidratosG() - dia.getCarbohidratosConsumidos()));
        macrosRestantes.setGrasasG(
                Math.max(0, macrosObjetivo.getGrasasG() - dia.getGrasasConsumidas()));

        /*------------------------
         *  Construcción del DTO
         *------------------------*/
        ResumenDiaDTO dto = new ResumenDiaDTO();
        dto.setFecha(LocalDate.now().toString());

        dto.setCaloriasObjetivo(caloriasObjetivo);
        dto.setCaloriasConsumidas(dia.getCaloriasConsumidas());
        dto.setCaloriasRestantes(caloriasRestantes);

        dto.setMacrosObjetivo(macrosObjetivo);

        Macros macrosConsumidos = new Macros();
        macrosConsumidos.setProteinasG(dia.getProteinasConsumidas());
        macrosConsumidos.setCarbohidratosG(dia.getCarbohidratosConsumidos());
        macrosConsumidos.setGrasasG(dia.getGrasasConsumidas());
        dto.setMacrosConsumidos(macrosConsumidos);

        dto.setMacrosRestantes(macrosRestantes);

        return dto;
    }
}