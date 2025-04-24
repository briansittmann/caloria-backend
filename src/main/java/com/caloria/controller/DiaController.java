package com.caloria.controller;

import com.caloria.dto.ResumenDiaDTO;
import com.caloria.service.DiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dia")
@RequiredArgsConstructor
public class DiaController {

    private final DiaService diaService;

    @GetMapping("/resumen/{usuarioId}")
    public ResumenDiaDTO obtenerResumenDelDia(@PathVariable String usuarioId) {
        return diaService.getResumenDelDia(usuarioId);
    }
}