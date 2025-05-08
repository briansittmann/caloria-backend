package com.caloria.controller;

import com.caloria.dto.*;
import com.caloria.model.Receta;
import com.caloria.model.Usuario;
import com.caloria.service.PerfilService;
import com.caloria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;
    private final PerfilService  perfilService;

    /**
     * Crea o actualiza todo el perfil de golpe.
     * POST /usuarios/perfil
     */
    @PostMapping
    public ResponseEntity<Usuario> crearOActualizarPerfil(
        @Valid @RequestBody PerfilUsuarioDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario perfil = usuarioService.crearOActualizar(uid, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(perfil);
    }

    /**
     * Actualiza todo el perfil de golpe.
     * PUT /usuarios/perfil
     */
    @PutMapping
    public ResponseEntity<Usuario> actualizarPerfil(
        @Valid @RequestBody PerfilUsuarioDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario perfil = usuarioService.crearOActualizar(uid, dto);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Paso 1 – datos básicos.
     * POST /usuarios/perfil/basicos
     */
    @PostMapping("/basicos")
    public ResponseEntity<Usuario> actualizarBasicos(
        @Valid @RequestBody BasicosDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario usuario = usuarioService.actualizarBasicos(uid, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    /**
     * Paso 2 – nivel de actividad.
     * POST /usuarios/perfil/actividad
     */
    @PostMapping("/actividad")
    public ResponseEntity<Usuario> actualizarActividad(
        @Valid @RequestBody ActividadDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario usuario = usuarioService.actualizarActividad(uid, dto.getNivelActividad());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Paso 3 – objetivo nutricional.
     * POST /usuarios/perfil/objetivo
     */
    @PostMapping("/objetivo")
    public ResponseEntity<Usuario> actualizarObjetivo(
        @Valid @RequestBody ObjetivoDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        // obtenemos nivelActividad desde el servicio para pasarlo al PerfilService
        String nivel = usuarioService.obtenerNivelActividad(uid);
        Usuario usuario = perfilService.completarObjetivo(uid, nivel, dto.getObjetivo());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Lee el perfil completo.
     * GET /usuarios/perfil
     */
    @GetMapping
    public ResponseEntity<Usuario> obtenerPerfil(Authentication auth) {
        String uid = auth.getName();
        Usuario perfil = usuarioService.obtenerPerfil(uid);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Solo indica si el perfil está completo.
     * GET /usuarios/perfil/completo
     */
    @GetMapping("/completo")
    public ResponseEntity<PerfilCompletoDTO> estaPerfilCompleto(Authentication auth) {
        String uid = auth.getName();
        boolean completo = usuarioService.obtenerPerfil(uid).isPerfilCompleto();
        return ResponseEntity.ok(new PerfilCompletoDTO(completo));
    }
    
   
    
}