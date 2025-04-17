package com.caloria.controller;

import com.caloria.service.IAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/comida")
public class ComidaController {

    @Autowired
    private IAService iaService;

    // Endpoint para analizar la comida
    @PostMapping("/analizar")
    public String analizarComida(@RequestBody Map<String, String> requestBody) throws InterruptedException {
        String descripcionComida = requestBody.get("descripcion");  
        // Llamamos al servicio para procesar la comida
        return iaService.analizarComida(descripcionComida);
    }
}