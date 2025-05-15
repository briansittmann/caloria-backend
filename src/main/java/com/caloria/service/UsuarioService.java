// src/main/java/com/caloria/service/UsuarioService.java
package com.caloria.service;

import com.caloria.dto.BasicosDTO;
import com.caloria.dto.PerfilUsuarioDTO;
import com.caloria.model.Receta;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository      usuarioRepo;
    private final CatalogoRecetasService catalogo;

    /* ------------------------------------------------------------ */
    /* Crea o actualiza todo el perfil de golpe                      */
    /* ------------------------------------------------------------ */
    public Usuario crearOActualizarPerfil(String uid, PerfilUsuarioDTO dto) {

        Usuario u = usuarioRepo.findById(uid)
            .orElseGet(() -> {
                log.debug("Creando usuario nuevo con id {}", uid);
                Usuario nuevo = new Usuario();
                nuevo.setId(uid);
                return nuevo;
            });

        // set de campos
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

        Usuario guardado = usuarioRepo.save(u);
        log.debug("Perfil completo guardado para uid={}", uid);
        return guardado;
    }

    /* ------------------------------------------------------------ */
    /* Crea un usuario esqueleto (registro)                         */
    /* ------------------------------------------------------------ */
    public Usuario crearUsuarioEsqueleto(String email) {
        Usuario u = new Usuario();
        u.setEmail(email);
        Usuario saved = usuarioRepo.save(u);
        log.debug("Usuario esqueleto creado uid={} email={}", saved.getId(), email);
        return saved;
    }

    /* ------------------------------------------------------------ */
    /* Lectura de perfil                                            */
    /* ------------------------------------------------------------ */
    public Usuario obtenerPerfil(String uid) {
        return usuarioRepo.findById(uid)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
    }

    /* ------------------------------------------------------------ */
    /* Paso 1: datos básicos                                        */
    /* ------------------------------------------------------------ */
    public Usuario actualizarBasicos(String uid, BasicosDTO dto) {
        Usuario u = obtenerPerfil(uid);

        u.setNombre(dto.getNombre());
        u.setEdad(dto.getEdad());
        u.setSexo(dto.getSexo());
        u.setPesoKg(dto.getPesoKg());
        u.setAlturaCm(dto.getAlturaCm());
        u.setHoraInicioDia(dto.getHoraInicioDia());
        u.setBasicosCompletos(true);

        Usuario saved = usuarioRepo.save(u);
        log.debug("Básicos completados para uid={}", uid);
        return saved;
    }

    /* ------------------------------------------------------------ */
    /* Paso 2: nivel de actividad                                   */
    /* ------------------------------------------------------------ */
    public Usuario actualizarNivelActividad(String uid, String nivel) {
        Usuario u = obtenerPerfil(uid);
        u.setNivelActividad(nivel);
        u.setActividadCompleta(true);
        Usuario saved = usuarioRepo.save(u);
        log.debug("Actividad '{}' guardada para uid={}", nivel, uid);
        return saved;
    }

    public String obtenerNivelActividad(String uid) {
        return obtenerPerfil(uid).getNivelActividad();
    }

    /* ------------------------------------------------------------ */
    /* Recetas                                                      */
    /* ------------------------------------------------------------ */
    public List<Receta> obtenerRecetasUsuario(String uid) {
        Usuario u = obtenerPerfil(uid);
        return catalogo.findAllByIds(u.getRecetas());
    }

    public List<Receta> guardarRecetasUsuario(String uid, List<Receta> nuevas) {
        Usuario u = obtenerPerfil(uid);

        Set<String> ids = new LinkedHashSet<>(u.getRecetas());
        nuevas.forEach(r -> ids.add(catalogo.saveIfNotExists(r).getId()));
        u.setRecetas(new ArrayList<>(ids));

        usuarioRepo.save(u);
        log.debug("{} recetas guardadas para uid={}", nuevas.size(), uid);
        return catalogo.findAllByIds(u.getRecetas());
    }

    public void eliminarRecetaUsuario(String uid, String recetaId) {
        Usuario u = obtenerPerfil(uid);
        if (u.getRecetas().remove(recetaId)) {
            usuarioRepo.save(u);
            log.debug("Receta {} eliminada de uid={}", recetaId, uid);
        }
    }
}