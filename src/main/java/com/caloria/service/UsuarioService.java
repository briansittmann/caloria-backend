package com.caloria.service;

import com.caloria.dto.UsuarioRequestDTO;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario crearUsuario(UsuarioRequestDTO usuarioDTO) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(usuarioDTO.getNombre());
        nuevoUsuario.setEmail(usuarioDTO.getEmail());
        nuevoUsuario.setEdad(usuarioDTO.getEdad());
        nuevoUsuario.setSexo(usuarioDTO.getSexo());
        nuevoUsuario.setAlturaCm(usuarioDTO.getAlturaCm());
        nuevoUsuario.setPesoKg(usuarioDTO.getPesoKg());
        nuevoUsuario.setNivelActividad(usuarioDTO.getNivelActividad());
        nuevoUsuario.setObjetivo(usuarioDTO.getObjetivo());
        nuevoUsuario.setCaloriasObjetivo(usuarioDTO.getCaloriasObjetivo());
        nuevoUsuario.setMacrosObjetivo(usuarioDTO.getMacrosObjetivo());
        nuevoUsuario.setHoraInicioDia(usuarioDTO.getHoraInicioDia());

        return usuarioRepository.save(nuevoUsuario);
    }
}