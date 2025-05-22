package com.caloria.controller;

import com.caloria.dto.ResumenDiaDTO;
import com.caloria.service.DiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


/**
 * Controlador REST encargado de exponer información diaria del usuario,
 * como el resumen de su progreso nutricional (calorías y macros).
 *
 * Este controlador opera sobre el día activo del usuario,
 * teniendo en cuenta su hora personalizada de inicio de jornada.
 *
 * Prefijo base: `/dia`
 */
@RestController
@RequestMapping("/dia")            
@RequiredArgsConstructor
public class DiaController {

    private final DiaService diaService;

    /**
     * Devuelve un resumen del progreso nutricional del usuario en su día actual.
     *
     * El resumen incluye:
     * - Calorías objetivo, consumidas y restantes.
     * - Macronutrientes objetivo, consumidos y restantes.
     * - Número de recetas generados por IA.
     *
     * El día nutricional se calcula dinámicamente usando la hora de inicio del usuario.
     *
     * @param authentication Token JWT (con el usuarioId como principal)
     * @return DTO con el resumen nutricional del día
     */
    @GetMapping("/resumen")
    public ResumenDiaDTO obtenerResumenDelDia(Authentication authentication) {
        String usuarioId = authentication.getName();
        return diaService.getResumenDelDia(usuarioId);
    }
}