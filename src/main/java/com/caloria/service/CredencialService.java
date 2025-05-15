// src/main/java/com/caloria/service/CredencialService.java
package com.caloria.service;

import com.caloria.dto.RegistroCredencialDTO;
import com.caloria.model.Credencial;
import com.caloria.model.Usuario;
import com.caloria.repository.CredencialRepository;
import com.caloria.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j                               // ← habilita log.debug / info / warn
@Service
@RequiredArgsConstructor
public class CredencialService {

    private final CredencialRepository credencialRepository;
    private final UsuarioRepository    usuarioRepository;
    private final PasswordEncoder      passwordEncoder;

    /* ------------------------------------------------------------------ */
    /* Registro: crea Credencial + Usuario vacío y devuelve la credencial */
    /* ------------------------------------------------------------------ */
    public Credencial registrar(RegistroCredencialDTO dto) {

        /* 1. email duplicado ------------------------------------------------ */
        if (credencialRepository.existsByEmail(dto.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", dto.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }

        /* 2. crear credencial ---------------------------------------------- */
        String uid = UUID.randomUUID().toString();

        Credencial cred = new Credencial();
        cred.setEmail(dto.getEmail());
        cred.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        cred.setRole("ROLE_USER");
        cred.setUsuarioId(uid);

        credencialRepository.save(cred);
        log.debug("Credencial guardada para {} (uid={})", cred.getEmail(), uid);

        /* 3. crear usuario esqueleto si no existe -------------------------- */
        if (!usuarioRepository.existsById(uid)) {
            Usuario usr = new Usuario();
            usr.setId(uid);
            usuarioRepository.save(usr);
            log.debug("Usuario esqueleto creado con id {}", uid);
        }

        return cred;
    }

    /* ------------------------------------------------------------------ */
    /* Validación de login: email + contraseña                            */
    /* ------------------------------------------------------------------ */
    public Credencial validar(String email, String rawPassword) {

        Credencial cred = credencialRepository.findByEmail(email);

        if (cred == null || !passwordEncoder.matches(rawPassword, cred.getPasswordHash())) {
            log.warn("Login fallido para {}", email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        log.debug("Login validado para {} (uid={})", email, cred.getUsuarioId());
        return cred;
    }

    /* Utilidad: buscar credencial por email ------------------------------ */
    public Credencial buscarPorEmail(String email) {
        return credencialRepository.findByEmail(email);
    }
}