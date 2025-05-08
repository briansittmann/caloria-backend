package com.caloria.service;

import com.caloria.dto.BasicosDTO;
import com.caloria.dto.PerfilUsuarioDTO;
import com.caloria.model.Receta;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CatalogoRecetasService catalogo;

    /** crea o actualiza el perfil asociado a usuarioId */
    public Usuario crearOActualizar(String usuarioId, PerfilUsuarioDTO dto) {
        Usuario usr = usuarioRepository.findById(usuarioId)
            .orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setId(usuarioId);
                return nuevo;
            });

        // … setters de los campos previos …
        usr.setNombre(dto.getNombre());
        usr.setEdad(dto.getEdad());
        usr.setSexo(dto.getSexo());
        usr.setAlturaCm(dto.getAlturaCm());
        usr.setPesoKg(dto.getPesoKg());
        usr.setNivelActividad(dto.getNivelActividad());
        usr.setObjetivo(dto.getObjetivo());
        usr.setCaloriasObjetivo(dto.getCaloriasObjetivo());
        usr.setMacrosObjetivo(dto.getMacrosObjetivo());
        usr.setHoraInicioDia(dto.getHoraInicioDia());

        usr.setPreferencias(dto.getPreferencias() != null
        	    ? dto.getPreferencias() : List.of());
        	usr.setAlergias(dto.getAlergias() != null
        	    ? dto.getAlergias() : List.of());

        usr.setPerfilCompleto(true);
        return usuarioRepository.save(usr);
    }

    public Usuario obtenerPerfil(String usuarioId) {
        return usuarioRepository.findById(usuarioId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }
    
    public Usuario actualizarBasicos(String usuarioId, BasicosDTO dto) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setNombre(dto.getNombre());
        u.setEdad(dto.getEdad());
        u.setSexo(dto.getSexo());
        u.setPesoKg(dto.getPesoKg());
        u.setAlturaCm(dto.getAlturaCm());
        u.setHoraInicioDia(dto.getHoraInicioDia());
        return usuarioRepository.save(u);
    }

    public Usuario actualizarActividad(String usuarioId, String nivelActividad) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        u.setNivelActividad(nivelActividad);
        return usuarioRepository.save(u);
    }

    public String obtenerNivelActividad(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(Usuario::getNivelActividad)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
    }
    
    /**
     * Añade al perfil del usuario las recetas recibidas desde la IA,
     * guardándolas primero en el catálogo si no existían.
     * Devuelve la lista de Receta completas que quedaron asociadas al usuario.
     */
    public List<Receta> guardarRecetasUsuario(String uid, List<Receta> nuevas) {
        // 1) Carga al usuario
        Usuario user = usuarioRepository.findById(uid)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + uid));

        // 2) Guarda cada nueva receta en el catálogo (si no existe) y obtiene su ID
        List<String> nuevosIds = nuevas.stream()
            .map(r -> catalogo.saveIfNotExists(r).getId())
            .collect(Collectors.toList());

        // 3) Añade esos IDs al array de recetas del usuario (sin duplicados)
        Set<String> mezclado = new LinkedHashSet<>();
        if (user.getRecetas() != null) mezclado.addAll(user.getRecetas());
        mezclado.addAll(nuevosIds);
        user.setRecetas(new ArrayList<>(mezclado));

        // 4) Persiste cambios en el usuario
        usuarioRepository.save(user);

        // 5) Recupera y devuelve las Receta completas asociadas
        return catalogo.findAllById(user.getRecetas());
    }

}