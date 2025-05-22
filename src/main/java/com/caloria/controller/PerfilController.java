package com.caloria.controller;

import com.caloria.dto.*;
import com.caloria.model.Usuario;
import com.caloria.repository.CredencialRepository;
import com.caloria.service.PerfilService;
import com.caloria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


/**
 * Controlador REST que gestiona la creación, actualización y verificación
 * del perfil de usuario en la aplicación.
 *
 * Coordina el flujo de onboarding por etapas (básicos, actividad, objetivo, preferencias)
 * y permite recalcular las metas nutricionales según el perfil actual.
 *
 * Prefijo base: `/usuarios/perfil`
 */
@RestController
@RequestMapping("/usuarios/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;
    private final PerfilService  perfilService;
    private final CredencialRepository credRepo;

    
    /**
     * Devuelve el perfil completo del usuario autenticado.
     * Incluye información física, preferencias, metas nutricionales y recetas.
     *
     * @param auth Autenticación JWT con el ID del usuario
     * @return Perfil de usuario
     */
    @GetMapping
    public ResponseEntity<Usuario> obtenerPerfil(Authentication auth) {
        String uid = auth.getName();
        Usuario perfil = usuarioService.obtenerPerfil(uid);

        // Volcar el email desde Credencial
        Optional.ofNullable(credRepo.findByUsuarioId(uid))
            .ifPresent(cred -> perfil.setEmail(cred.getEmail()));

        return ResponseEntity.ok(perfil);
    }
    
    
    
    /**
     * Crea o actualiza de forma masiva el perfil del usuario.
     * Útil cuando se completa todo el onboarding de una sola vez.
     *
     * @param dto DTO con todos los campos del perfil
     * @param auth Autenticación JWT
     * @return Perfil actualizado
     */
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<Usuario> upsertPerfil(
        @Valid @RequestBody PerfilUsuarioDTO dto,
        Authentication auth
    ) {
      String uid = auth.getName();
      Usuario saved = usuarioService.crearOActualizarPerfil(uid, dto);
      return ResponseEntity.ok(saved);
    }
    
    
    /**
     * Actualiza todo el perfil del usuario (alternativa explícita por método PUT).
     *
     * @param dto Datos del perfil completo
     * @param auth Token JWT del usuario
     * @return Usuario actualizado
     */
    @PutMapping
    public ResponseEntity<Usuario> actualizarPerfil(
        @Valid @RequestBody PerfilUsuarioDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario perfil = usuarioService.crearOActualizarPerfil(uid, dto);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Paso 1 – Registra los datos físicos básicos del usuario:
     * nombre, edad, sexo, altura, peso y hora de inicio del día.
     *
     * @param dto Datos básicos
     * @param auth Autenticación del usuario
     * @return Usuario con básicos completados
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
     * Paso 2 – Actualiza el nivel de actividad física del usuario.
     *
     * @param dto DTO con el nivel de actividad
     * @param auth Autenticación del usuario
     * @return Usuario actualizado con actividad completada
     */
    @PostMapping("/actividad")
    public ResponseEntity<Usuario> actualizarActividad(
        @Valid @RequestBody ActividadDTO dto,
        Authentication auth
    ) {
        String uid = auth.getName();
        Usuario usuario = usuarioService.actualizarNivelActividad(uid, dto.getNivelActividad());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Paso 3 – Registra el objetivo nutricional del usuario (cut, mantenimiento, bulk)
     * y calcula sus calorías y macros objetivo.
     *
     * @param dto Objetivo nutricional elegido
     * @param auth Autenticación del usuario
     * @return Usuario con metas aplicadas
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
     * Paso 4 – Guarda las preferencias alimenticias y alergias del usuario.
     * Marca el paso como completo y revisa si todo el perfil ya está configurado.
     *
     * @param dto Preferencias y alergias
     * @param auth Token JWT
     * @return Usuario actualizado
     */
    @PutMapping("/preferencias")
    public ResponseEntity<Usuario> actualizarPreferencias(
        @Valid @RequestBody PreferenciasDTO dto,
        Authentication auth
    ) {
      String uid = auth.getName();
      // Este método sí marca preferenciasCompleto **y** revisa todo el perfil
      Usuario u = perfilService.marcarPreferenciasCompleto(uid, dto);
      return ResponseEntity.ok(u);
    }
    
    
    /**
     * Fuerza el recálculo de las metas nutricionales (BMR, TDEE, calorías, macros),
     * utilizando los datos actuales del perfil.
     *
     * @param auth Autenticación del usuario
     * @return Usuario con metas recalculadas
     */
    @PutMapping("/recalcular-metas")
    public ResponseEntity<Usuario> recalcularMetas(Authentication auth) {
        String uid = auth.getName();
        Usuario actualizado = perfilService.recalcularMetas(uid);
        return ResponseEntity.ok(actualizado);
    }
    
    /**
     * Verifica si el perfil del usuario está completamente configurado.
     * Es decir, si los 4 pasos del onboarding están completos.
     *
     * @param auth Autenticación del usuario
     * @return true si el perfil está completo
     */
    @GetMapping("/completo")
    public ResponseEntity<PerfilCompletoDTO> estaPerfilCompleto(Authentication auth) {
        String uid = auth.getName();
        boolean completo = usuarioService.obtenerPerfil(uid).isPerfilCompleto();
        return ResponseEntity.ok(new PerfilCompletoDTO(completo));
    }
    
    /**
     * Devuelve el estado detallado del progreso del perfil,
     * indicando qué pasos se han completado y si el perfil está listo.
     *
     * @param auth Autenticación del usuario
     * @return DTO con flags de estado por fase
     */
    @GetMapping("/estado")
    public ResponseEntity<PerfilEstadoDTO> estadoPerfil(Authentication auth) {
      Usuario u = usuarioService.obtenerPerfil(auth.getName());
      return ResponseEntity.ok(new PerfilEstadoDTO(
        u.getNombre()       != null && !u.getNombre().isBlank()
      , u.getNivelActividad() != null
      , u.getObjetivo()       != null
      , u.getPreferencias()   != null && !u.getPreferencias().isEmpty()
      , u.isPerfilCompleto()
      ));
    }
    
}