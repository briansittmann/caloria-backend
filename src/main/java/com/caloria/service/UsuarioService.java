package com.caloria.service;

import com.caloria.dto.UsuarioRequestDTO;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor para inicializar BCryptPasswordEncoder
    public UsuarioService() {
        this.passwordEncoder = new BCryptPasswordEncoder(); // Instanciamos BCryptPasswordEncoder
    }

    // Método para crear un nuevo usuario con la contraseña cifrada
    public Usuario crearUsuario(UsuarioRequestDTO usuarioDTO) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }

        // Crear el nuevo usuario
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

        // Cifrar la contraseña con BCrypt
        String hashedPassword = passwordEncoder.encode(usuarioDTO.getPassword());
        nuevoUsuario.setPassword(hashedPassword);  // Guardamos la contraseña cifrada

        return usuarioRepository.save(nuevoUsuario);  // Guardamos el nuevo usuario en la base de datos
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Método para validar las credenciales del login
    public boolean validarCredenciales(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        // Comprobamos si el usuario existe y si la contraseña proporcionada coincide con la almacenada
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            return true;  // Las credenciales son correctas
        }

        return false;  // Las credenciales no son válidas
    }
}