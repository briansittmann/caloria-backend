package com.caloria.controller;

import com.caloria.dto.ResumenDiaDTO;
import com.caloria.service.DiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dia")            
@RequiredArgsConstructor
public class DiaController {

    private final DiaService diaService;

    /**
     * GET /dia/resumen
     * El JWT AuthenticationFilter ya puso el usuarioId como nombre del principal,
     * así que lo sacamos de Authentication.getName().
     */
    @GetMapping("/resumen")
    public ResumenDiaDTO obtenerResumenDelDia(Authentication authentication) {
        String usuarioId = authentication.getName();
        return diaService.getResumenDelDia(usuarioId);
    }
}