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



/**
 * Servicio encargado de gestionar la configuración y completitud del perfil nutricional del usuario.
 *
 * Permite marcar como completados los pasos del onboarding (datos básicos, nivel de actividad, objetivo, preferencias)
 * y calcular automáticamente las metas calóricas y macronutrientes cuando se selecciona un objetivo nutricional.
 *
 * Colabora estrechamente con el {@link MetabolismoService} para realizar los cálculos personalizados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepo;
    private final MetabolismoService metabolismo;


    /**
     * Si el usuario ya ha indicado nivel de actividad y objetivo nutricional,
     * recalcula sus calorías y macros objetivo.
     *
     * @param usuarioId ID del usuario
     * @return Usuario actualizado (no guardado si no recalcula)
     */
    public Usuario recalcularMetas(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        if (u.getNivelActividad() != null && u.getObjetivo() != null) {
            completarObjetivo(usuarioId, u.getNivelActividad(), u.getObjetivo());
        }
        return u;
    }

    /**
     * Marca como completado el paso de datos básicos y revisa si el perfil está completo.
     */
    public Usuario marcarBasicosCompletos(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setBasicosCompletos(true);
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }

    /**
     * Marca como completado el paso de actividad física y revisa si el perfil está completo.
     */
    public Usuario marcarActividadCompleta(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setActividadCompleta(true);
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }

    /**
     * Marca como completado el paso de objetivo nutricional y revisa si el perfil está completo.
     */
    public Usuario marcarObjetivoCompleto(String usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setObjetivoCompleto(true);
        log.debug(">> completarObjetivo: antes de marcar, objetivoCompleto = {}", u.isObjetivoCompleto());
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }
    
    
    /**
     * Guarda las preferencias y alergias del usuario, marca ese paso como completado
     * y verifica si ya se cumplen las condiciones para considerar el perfil como completo.
     *
     * @param usuarioId ID del usuario
     * @param dto DTO con listas de preferencias y alergias
     * @return Usuario actualizado
     */
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
     * Calcula las calorías y macronutrientes objetivo a partir del peso, altura, edad, sexo,
     * nivel de actividad física y objetivo nutricional del usuario.
     *
     * Este método es el paso final del onboarding y marca automáticamente el perfil como completo si corresponde.
     *
     * @param usuarioId ID del usuario
     * @param nivelActStr Nivel de actividad (valor del enum NivelActividad)
     * @param objetivoStr Objetivo nutricional (valor del enum ObjetivoNutricional)
     * @return Usuario actualizado con metas aplicadas
     */
    public Usuario completarObjetivo(String usuarioId, String nivelActStr, String objetivoStr) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        // 1️⃣ Calcula BMR → TDEE → Calorías objetivo → Macros
        double bmr = metabolismo.calcularBmr(u.getPesoKg(), u.getAlturaCm(), u.getEdad(), u.getSexo());
        NivelActividad nivel = NivelActividad.valueOf(nivelActStr.toUpperCase());
        double tdee = metabolismo.calcularTdee(bmr, nivel);
        ObjetivoNutricional obj = ObjetivoNutricional.valueOf(objetivoStr.toUpperCase());
        double caloriasMeta = metabolismo.calcularCaloriasObjetivo(tdee, obj);
        Macros macrosMeta = metabolismo.calcularMacrosObjetivo(caloriasMeta);
        
        // 2️⃣ Aplica las metas al usuario
        u.setObjetivo(objetivoStr);
        u.aplicarMetas(caloriasMeta, macrosMeta);

        // 2️⃣ Marco el paso de objetivo completado y reviso todo el perfil
        u.setObjetivoCompleto(true);
        revisarPerfilCompleto(u);

        // 3️⃣ Guarda el estado final completo
        return usuarioRepo.save(u);
      }
    
    
    /**
     * Verifica si el perfil del usuario está completamente configurado.
     * Esto ocurre cuando se han completado los pasos de:
     * básicos, actividad, objetivo y preferencias.
     *
     * Si el perfil está completo, se marca el flag correspondiente.
     *
     * @param u Objeto Usuario a evaluar y modificar
     */
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