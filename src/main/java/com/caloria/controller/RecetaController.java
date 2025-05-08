// src/main/java/com/caloria/controller/RecetaController.java
package com.caloria.controller;

import com.caloria.dto.MacrosDTO;
import com.caloria.dto.RecetaRequestDTO;
import com.caloria.dto.ResumenDiaDTO;
import com.caloria.model.Macros;
import com.caloria.model.Usuario;
import com.caloria.service.DiaService;
import com.caloria.service.IAService;
import com.caloria.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final IAService iaService;
    private final DiaService diaService;
    private final UsuarioService usuarioService; // si sacas preferencias/alergias de aquí

    @PostMapping("/generar")
    public String generarRecetas(
            @RequestParam int numComidas,
            Authentication auth) throws InterruptedException {

        String uid = auth.getName();

        // 1) Obtenemos resumen del día
        ResumenDiaDTO resumen = diaService.getResumenDelDia(uid);

        // 2) Sacamos el modelo Macros de "macrosRestantes" y la calorías
        Macros restos = resumen.getMacrosRestantes();
        double caloriasRest = resumen.getCaloriasRestantes();

        // 3) Convertimos a DTO (necesario para el servicio de IA)
        MacrosDTO macrosDto = new MacrosDTO(
            restos.getProteinasG(),
            restos.getCarbohidratosG(),
            restos.getGrasasG(),
            caloriasRest
        );

        // 4) Sacamos preferencias y alergias del usuario (o de donde las guardes)
        Usuario usr = usuarioService.obtenerPerfil(uid);
        List<String> prefs = usr.getPreferencias();   // asegúrate de que existen getter
        List<String> alerg = usr.getAlergias();

        // 5) Llamada al IAService con los tipos correctos
        return iaService.generarRecetas(
            prefs,
            alerg,
            macrosDto,
            numComidas  // Integer → int automáticamente
        );
    }
}
