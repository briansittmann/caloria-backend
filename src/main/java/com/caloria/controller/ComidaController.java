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


/**
 * Controlador REST encargado del análisis y registro de comidas ingresadas por el usuario.
 *
 * Permite analizar alimentos usando IA y registrar sus macronutrientes diarios,
 * así como importar alimentos al catálogo central.
 *
 * Prefijo base: `/comida`
 */
@RestController
@RequestMapping("/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final IAService iaService;
    private final CatalogoAlimentoService catalogoService;
    
    
    /**
     * Analiza una lista de alimentos proporcionados por el usuario.
     *
     * Si los alimentos ya están en el catálogo, se usan sus valores nutricionales.
     * Si no están, se consulta a la IA para estimar los macros y luego se registran.
     *
     * Los resultados se devuelven como un JSON con los nutrientes por alimento.
     *
     * @param dtos Lista de alimentos con nombre y gramos
     * @param auth Token JWT del usuario
     * @return JSON con los macronutrientes calculados para cada alimento
     * @throws InterruptedException si el hilo de análisis es interrumpido
     */
    @PostMapping("/analizar")
    public ResponseEntity<String> analizarComida(
            @RequestBody List<AlimentoDTO> dtos,
            Authentication auth) throws InterruptedException {

        String usuarioId = auth.getName();
        String json = iaService.analizarComida(dtos, usuarioId);
        return ResponseEntity.ok(json);
    }
    
    /**
     * Importa un listado de alimentos al catálogo general desde un array JSON.
     *
     * Cada alimento se guarda sólo si no existe uno con el mismo nombre (case-insensitive).
     * Este endpoint es útil para inicializar o poblar el sistema con nuevos alimentos.
     *
     * @param catalogoJson Lista de objetos CatalogoAlimento
     * @return Lista de alimentos realmente insertados o ya existentes
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