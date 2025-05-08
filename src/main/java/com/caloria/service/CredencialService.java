package com.caloria.service;

import com.caloria.dto.RegistroCredencialDTO;
import com.caloria.model.Credencial;
import com.caloria.model.Usuario;
import com.caloria.repository.CredencialRepository;
import com.caloria.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor            // inyección por constructor
public class CredencialService {

    private final CredencialRepository credencialRepository;
    private final UsuarioRepository    usuarioRepository; 
    private final PasswordEncoder passwordEncoder;   // ⬅️ se inyecta el bean definido en SecurityConfig

    /**
     * Registro paso 1 : crea la credencial (email + hash) y devuelve el objeto guardado.
     * Genera un usuarioId nuevo y asigna rol USER por defecto.
     */
    public Credencial registrar(RegistroCredencialDTO dto) {

        // 1. comprobar email duplicado
        if (credencialRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }

        // 2. crear y guardar la credencial
        String uid = UUID.randomUUID().toString();

        Credencial cred = new Credencial();
        cred.setEmail(dto.getEmail());
        cred.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        cred.setRole("ROLE_USER");
        cred.setUsuarioId(uid);

        credencialRepository.save(cred);

        // 3. crear Usuario vacío si no existe
        if (!usuarioRepository.existsById(uid)) {
            Usuario usr = new Usuario();
            usr.setId(uid);
            usuarioRepository.save(usr);
        }

        return cred;
    }

    /**
     * Valida email + contraseña; si es correcto devuelve la Credencial,
     * si no lanza 401 UNAUTHORIZED.
     */
    public Credencial validar(String email, String rawPassword) {

        Credencial cred = credencialRepository.findByEmail(email);

        if (cred == null || !passwordEncoder.matches(rawPassword, cred.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        return cred;
    }

    /** Utilidad opcional para consultas internas */
    public Credencial buscarPorEmail(String email) {
        return credencialRepository.findByEmail(email);
    }
}