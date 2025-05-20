// src/main/java/com/caloria/controller/ComidaController.java
package com.caloria.controller;

import com.caloria.dto.AlimentoDTO;
import com.caloria.service.IAService;
import lombok.RequiredArgsConstructor;
import com.caloria.model.CatalogoAlimento;
import com.caloria.service.CatalogoAlimentoService;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final IAService iaService;
    private final CatalogoAlimentoService catalogoService;

    @PostMapping("/analizar")
    public ResponseEntity<String> analizarComida(
            @RequestBody List<AlimentoDTO> dtos,
            Authentication auth) throws InterruptedException {

        String usuarioId = auth.getName();
        String json = iaService.analizarComida(dtos, usuarioId);
        return ResponseEntity.ok(json);
    }
    
    /**
     * Importa un array JSON de CatalogoAlimento y guarda cada uno si no existía ya.
     */
    @PostMapping("/importar-catalogo")
    public ResponseEntity<List<CatalogoAlimento>> importarCatalogo(
            @RequestBody List<CatalogoAlimento> catalogoJson) {

        List<CatalogoAlimento> resultado = catalogoJson.stream()
            .map(catalogoService::guardarSiNoExisteCatalogo)
            .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);
    }
}