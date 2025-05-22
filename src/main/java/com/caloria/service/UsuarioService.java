// src/main/java/com/caloria/service/UsuarioService.java
package com.caloria.service;

import com.caloria.dto.BasicosDTO;
import com.caloria.dto.PerfilUsuarioDTO;
import com.caloria.dto.PreferenciasDTO;
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

/**
 * Servicio principal para la gestión de usuarios, enfocado en la creación,
 * actualización y lectura de perfiles nutricionales, así como el manejo
 * de recetas guardadas por el usuario.
 *
 * Este servicio expone métodos que reflejan los pasos del flujo de onboarding:
 * 1. Datos básicos
 * 2. Nivel de actividad
 * 3. Objetivo (calculado externamente)
 * 4. Preferencias y alergias
 *
 * También permite registrar recetas favoritas del usuario.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository      usuarioRepo;
    private final CatalogoRecetasService catalogo;

    /**
     * Paso global para crear o actualizar un perfil completo en una única operación.
     * Se utiliza principalmente en flujos rápidos donde el usuario ya conoce sus datos.
     *
     * @param uid ID del usuario
     * @param dto DTO con todos los campos del perfil
     * @return Usuario actualizado o creado
     */
    public Usuario crearOActualizarPerfil(String uid, PerfilUsuarioDTO dto) {

        Usuario u = usuarioRepo.findById(uid)
            .orElseGet(() -> {
                log.debug("Creando usuario nuevo con id {}", uid);
                Usuario nuevo = new Usuario();
                nuevo.setId(uid);
                return nuevo;
            });

        // Asignación de todos los campos del perfil
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

        Usuario guardado = usuarioRepo.save(u);
        log.debug("Perfil completo guardado para uid={}", uid);
        return guardado;
    }

    /**
     * Crea un usuario "esqueleto" (sólo con email), utilizado en el registro inicial.
     *
     * @param email Email del nuevo usuario
     * @return Usuario persistido
     */
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

    /**
     * Paso 1 del onboarding: registra los datos físicos básicos del usuario.
     * Marca el paso como completado.
     */
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

    /**
     * Paso 2 del onboarding: guarda el nivel de actividad del usuario.
     * Marca el paso como completado.
     */
    public Usuario actualizarNivelActividad(String uid, String nivel) {
        Usuario u = obtenerPerfil(uid);
        u.setNivelActividad(nivel);
        u.setActividadCompleta(true);
        Usuario saved = usuarioRepo.save(u);
        log.debug("Actividad '{}' guardada para uid={}", nivel, uid);
        return saved;
    }
    
    /**
     * Devuelve el nivel de actividad registrado por el usuario.
     */
    public String obtenerNivelActividad(String uid) {
        return obtenerPerfil(uid).getNivelActividad();
    }
    
    /**
     * Paso 4 del onboarding: guarda las preferencias y alergias alimenticias del usuario.
     * Marca el paso como completado.
     *
     * @param uid ID del usuario
     * @param dto Preferencias y alergias
     * @return Usuario actualizado
     */
    public Usuario actualizarPreferencias(String uid, PreferenciasDTO dto) {
        // 1) Recupera el usuario (lanza 404 si no existe)
        Usuario u = obtenerPerfil(uid);

        // 2) Setea los nuevos arrays
        u.setPreferencias(dto.getPreferencias());
        u.setAlergias(dto.getAlergias());
        u.setPreferenciasCompletas(true);

        // 3) Guarda y loggea
        Usuario saved = usuarioRepo.save(u);
        log.debug("Preferencias guardadas para uid={}", uid);
        return saved;
    }
    
    
    /**
     * Devuelve la lista de recetas guardadas por el usuario.
     */
    public List<Receta> obtenerRecetasUsuario(String uid) {
        Usuario u = obtenerPerfil(uid);
        return catalogo.findAllByIds(u.getRecetas());
    }
    
    
    /**
     * Guarda nuevas recetas en la lista del usuario, evitando duplicados.
     *
     * @param uid ID del usuario
     * @param nuevas Lista de recetas a agregar
     * @return Lista actualizada de recetas del usuario
     */
    public List<Receta> guardarRecetasUsuario(String uid, List<Receta> nuevas) {
        Usuario u = obtenerPerfil(uid);

        Set<String> ids = new LinkedHashSet<>(u.getRecetas());
        nuevas.forEach(r -> ids.add(catalogo.saveIfNotExists(r).getId()));
        u.setRecetas(new ArrayList<>(ids));

        usuarioRepo.save(u);
        log.debug("{} recetas guardadas para uid={}", nuevas.size(), uid);
        return catalogo.findAllByIds(u.getRecetas());
    }
    
    
    /**
     * Elimina una receta de la lista del usuario.
     *
     * @param uid ID del usuario
     * @param recetaId ID de la receta a eliminar
     */
    public void eliminarRecetaUsuario(String uid, String recetaId) {
        Usuario u = obtenerPerfil(uid);
        if (u.getRecetas().remove(recetaId)) {
            usuarioRepo.save(u);
            log.debug("Receta {} eliminada de uid={}", recetaId, uid);
        }
    }
    
    
}