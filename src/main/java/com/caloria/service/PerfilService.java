// src/main/java/com/caloria/service/PerfilService.java
package com.caloria.service;

import com.caloria.model.Macros;
import com.caloria.model.NivelActividad;
import com.caloria.model.ObjetivoNutricional;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepo;
    private final MetabolismoService metabolismo;

    /**
     * Paso final: calcula y guarda calorías y macros meta para un usuario
     */
    public Usuario completarObjetivo(String usuarioId, String nivelActStr, String objetivoStr) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        double bmr = metabolismo.calcularBmr(u.getPesoKg(), u.getAlturaCm(), u.getEdad(), u.getSexo());
        NivelActividad nivel = NivelActividad.valueOf(nivelActStr.toUpperCase());
        double tdee = metabolismo.calcularTdee(bmr, nivel);
        ObjetivoNutricional obj = ObjetivoNutricional.valueOf(objetivoStr.toUpperCase());
        double caloriasMeta = metabolismo.calcularCaloriasObjetivo(tdee, obj);
        Macros macrosMeta = metabolismo.calcularMacrosObjetivo(caloriasMeta);

        u.setObjetivo(objetivoStr);
        u.aplicarMetas(caloriasMeta, macrosMeta);
        marcarObjetivoCompleto(u.getId());

        return usuarioRepo.save(u);
    }
    

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
        revisarPerfilCompleto(u);
        return usuarioRepo.save(u);
    }

    private void revisarPerfilCompleto(Usuario u) {
        if (u.isBasicosCompletos() &&
            u.isActividadCompleta() &&
            u.isObjetivoCompleto() &&
            u.isPreferenciasCompletas()) {
            u.setPerfilCompleto(true);
            // si aún no se han aplicado metas:
            completarObjetivo(u.getId(), u.getNivelActividad(), u.getObjetivo());
        } else {
            u.setPerfilCompleto(false);
        }
    }
}