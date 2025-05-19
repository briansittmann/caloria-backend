// src/main/java/com/caloria/service/PerfilService.java
package com.caloria.service;
import lombok.extern.slf4j.Slf4j;

import com.caloria.dto.PreferenciasDTO;
import com.caloria.model.Macros;
import com.caloria.model.NivelActividad;
import com.caloria.model.ObjetivoNutricional;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepo;
    private final MetabolismoService metabolismo;


    
    /**
     * Recalcula títulos y macros de un usuario si ya tiene actividad y objetivo
     */
    public Usuario recalcularMetas(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        if (u.getNivelActividad() != null && u.getObjetivo() != null) {
            completarObjetivo(usuarioId, u.getNivelActividad(), u.getObjetivo());
        }
        return u;
    }

    /** Marca que el paso de básicos está completado y revisa si el perfil ya está completo */
    public Usuario marcarBasicosCompletos(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setBasicosCompletos(true);
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }

    /** Marca que el paso de actividad está completado y revisa si el perfil ya está completo */
    public Usuario marcarActividadCompleta(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setActividadCompleta(true);
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }

    /** Marca que el paso de objetivo está completado y revisa si el perfil ya está completo */
    public Usuario marcarObjetivoCompleto(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setObjetivoCompleto(true);
        log.debug(">> completarObjetivo: antes de marcar, objetivoCompleto = {}", u.isObjetivoCompleto());
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }
    
    /** Marca que el paso de preferencias está completado y revisa si el perfil ya está completo */
    public Usuario marcarPreferenciasCompleto(String usuarioId, PreferenciasDTO dto) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        log.debug(">>> marcarPreferenciasCompleto IN: preferencias={} alergias={} antes perfilCompleto={}",
                  dto.getPreferencias(), dto.getAlergias(), u.isPerfilCompleto());

        u.setPreferencias(dto.getPreferencias());
        u.setAlergias(dto.getAlergias());
        u.setPreferenciasCompletas(true);

        revisarPerfilCompleto(u);
        Usuario saved = usuarioRepo.save(u);

        log.debug(">>> marcarPreferenciasCompleto OUT: preferenciasCompletas={} perfilCompleto={} tras save",
                  saved.isPreferenciasCompletas(), saved.isPerfilCompleto());
        return saved;
      }
    
    /**
     * Paso final: calcula y guarda calorías y macros meta para un usuario
     */
    public Usuario completarObjetivo(String usuarioId, String nivelActStr, String objetivoStr) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        // 1️⃣ Aplico las metas
        double bmr = metabolismo.calcularBmr(u.getPesoKg(), u.getAlturaCm(), u.getEdad(), u.getSexo());
        NivelActividad nivel = NivelActividad.valueOf(nivelActStr.toUpperCase());
        double tdee = metabolismo.calcularTdee(bmr, nivel);
        ObjetivoNutricional obj = ObjetivoNutricional.valueOf(objetivoStr.toUpperCase());
        double caloriasMeta = metabolismo.calcularCaloriasObjetivo(tdee, obj);
        Macros macrosMeta = metabolismo.calcularMacrosObjetivo(caloriasMeta);

        u.setObjetivo(objetivoStr);
        u.aplicarMetas(caloriasMeta, macrosMeta);

        // 2️⃣ Marco el paso de objetivo completado y reviso todo el perfil
        u.setObjetivoCompleto(true);
        revisarPerfilCompleto(u);

        // 3️⃣ Guardo UNA sola vez (con calorías, macros, objetivoCompleto y perfilCompleto bien calculados)
        return usuarioRepo.save(u);
      }
    
    

    private void revisarPerfilCompleto(Usuario u) {
        boolean completo =
          u.isBasicosCompletos() &&
          u.isActividadCompleta() &&
          u.isObjetivoCompleto() &&
          u.isPreferenciasCompletas();

        log.debug("    revisarPerfilCompleto: basicos={} actividad={} objetivo={} preferencias={} → completo={}",
                  u.isBasicosCompletos(), u.isActividadCompleta(),
                  u.isObjetivoCompleto(), u.isPreferenciasCompletas(),
                  completo);

        u.setPerfilCompleto(completo);
        if (completo) {
          log.debug("    PERFIL YA COMPLETO");
        }
    }
}