// src/main/java/com/caloria/service/UsuarioService.java
package com.caloria.service;

import com.caloria.dto.BasicosDTO;
import com.caloria.dto.PerfilUsuarioDTO;
import com.caloria.model.Receta;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final CatalogoRecetasService catalogo;

    /** Crea o actualiza todo el perfil de golpe */
    public Usuario crearOActualizarPerfil(String usuarioId, PerfilUsuarioDTO dto) {
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setId(usuarioId);
                return nuevo;
            });

        u.setNombre(dto.getNombre());
        u.setEdad(dto.getEdad());
        u.setSexo(dto.getSexo());
        u.setAlturaCm(dto.getAlturaCm());
        u.setPesoKg(dto.getPesoKg());
        u.setNivelActividad(dto.getNivelActividad());
        u.setObjetivo(dto.getObjetivo());
        u.setHoraInicioDia(dto.getHoraInicioDia());
        u.setPreferencias(dto.getPreferencias());
        u.setAlergias(dto.getAlergias());

        u.setPerfilCompleto(true);
        return usuarioRepo.save(u);
    }

    /** Obtiene el perfil */
    public Usuario obtenerPerfil(String usuarioId) {
        return usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
    }

    /** Actualiza solo datos básicos */
    public Usuario actualizarBasicos(String usuarioId, BasicosDTO dto) {
        Usuario u = obtenerPerfil(usuarioId);
        u.setNombre(dto.getNombre());
        u.setEdad(dto.getEdad());
        u.setSexo(dto.getSexo());
        u.setPesoKg(dto.getPesoKg());
        u.setAlturaCm(dto.getAlturaCm());
        u.setHoraInicioDia(dto.getHoraInicioDia());
        u.setBasicosCompletos(true);
        return usuarioRepo.save(u);
    }

    /** Actualiza solo nivel de actividad */
    public Usuario actualizarNivelActividad(String usuarioId, String nivel) {
        Usuario u = obtenerPerfil(usuarioId);
        u.setNivelActividad(nivel);
        u.setActividadCompleta(true);
        return usuarioRepo.save(u);
    }
    
    /** Devuelve el nivel de actividad actual (o null si no está definido) */
    public String obtenerNivelActividad(String usuarioId) {
        return obtenerPerfil(usuarioId).getNivelActividad();
    }

    /** Devuelve todas las recetas del usuario */
    public List<Receta> obtenerRecetasUsuario(String usuarioId) {
        Usuario u = obtenerPerfil(usuarioId);
        return catalogo.findAllByIds(u.getRecetas());
    }

    /** Guarda nuevas recetas en el perfil sin duplicados */
    public List<Receta> guardarRecetasUsuario(String usuarioId, List<Receta> nuevas) {
        Usuario u = obtenerPerfil(usuarioId);
        Set<String> ids = new LinkedHashSet<>(u.getRecetas());
        nuevas.forEach(r -> ids.add(catalogo.saveIfNotExists(r).getId()));
        u.setRecetas(new ArrayList<>(ids));
        usuarioRepo.save(u);
        return catalogo.findAllByIds(u.getRecetas());
    }

    /** Elimina una receta del perfil */
    public void eliminarRecetaUsuario(String usuarioId, String recetaId) {
        Usuario u = obtenerPerfil(usuarioId);
        if (u.getRecetas().remove(recetaId)) {
            usuarioRepo.save(u);
        }
    }
}