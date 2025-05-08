// src/main/java/com/caloria/controller/ComidaController.java
package com.caloria.controller;

import com.caloria.dto.AlimentoDTO;
import com.caloria.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final IAService iaService;

    @PostMapping("/analizar")
    public String analizarComida(
            @RequestBody AlimentoDTO dto,
            Authentication auth) throws InterruptedException {

        String usuarioId = auth.getName();
        // dto.getGramos() es double, ahora el servicio lo recibe como double
        return iaService.analizarComida(
            dto.getNombre(),
            dto.getGramos(),
            usuarioId
        );
    }
}