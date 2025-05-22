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


/**
 * Servicio responsable de la gestión de credenciales de usuario,
 * incluyendo el registro seguro (con encriptación de contraseñas),
 * la validación de login, y la sincronización con la entidad Usuario.
 *
 * Este servicio coordina las credenciales (email, contraseña encriptada y rol)
 * con la creación de un usuario "esqueleto" vinculado mediante UUID.
 */
@Slf4j                               // ← habilita log.debug / info / warn
@Service
@RequiredArgsConstructor
public class CredencialService {

    private final CredencialRepository credencialRepository;
    private final UsuarioRepository    usuarioRepository;
    private final PasswordEncoder      passwordEncoder;

    /**
     * Registra una nueva credencial en el sistema, generando:
     * 1) Un nuevo UUID como identificador.
     * 2) Un objeto Credencial persistido con contraseña encriptada.
     * 3) Un usuario esqueleto asociado al mismo UUID, con el email.
     *
     * Lanza una excepción si el email ya está registrado.
     *
     * @param dto Datos del registro (email y contraseña en texto plano)
     * @return Credencial registrada y persistida
     */
    public Credencial registrar(RegistroCredencialDTO dto) {

        // 1. Validar duplicado
        if (credencialRepository.existsByEmail(dto.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", dto.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }

        // 2. Crear credencial
        String uid = UUID.randomUUID().toString();
        Credencial cred = new Credencial();
        cred.setEmail(dto.getEmail());
        cred.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        cred.setRole("ROLE_USER");
        cred.setUsuarioId(uid);
        credencialRepository.save(cred);
        log.debug("Credencial guardada para {} (uid={})", cred.getEmail(), uid);

        // 3. Crear usuario esqueleto con email
        if (!usuarioRepository.existsById(uid)) {
            Usuario usr = new Usuario();
            usr.setId(uid);
            usr.setEmail(dto.getEmail());               // También guardamos el email en la entidad Usuario
            usuarioRepository.save(usr);
            log.debug("Usuario esqueleto creado con id {} y email={}", uid, dto.getEmail());
        }

        return cred;
    }

    /**
     * Valida el intento de inicio de sesión verificando que:
     * - El email esté registrado
     * - La contraseña ingresada coincida con el hash almacenado
     *
     * Lanza una excepción HTTP 401 si las credenciales son incorrectas.
     *
     * @param email Email ingresado
     * @param rawPassword Contraseña en texto plano
     * @return Credencial válida si la autenticación fue exitosa
     */
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

    /**
     * Devuelve la credencial correspondiente al email dado, o null si no existe.
     *
     * @param email Email a buscar
     * @return Credencial encontrada o null
     */
    public Credencial buscarPorEmail(String email) {
        return credencialRepository.findByEmail(email);
    }
}