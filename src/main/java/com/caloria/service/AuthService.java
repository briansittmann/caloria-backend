package com.caloria.service;

import com.caloria.dto.RegistroCredencialDTO;
import com.caloria.model.Credencial;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Servicio encargado de la autenticación de usuarios.
 *
 * Expone métodos para el inicio de sesión (login) y el registro,
 * integrando la validación de credenciales con la generación de tokens JWT.
 *
 * Coordina con:
 * - {@link CredencialService} para la gestión de credenciales y usuarios
 * - {@link JwtService} para la generación segura de tokens firmados
 */
@Slf4j  
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredencialService credencialService;   // Maneja email + BCrypt
    
    private final JwtService jwtService;  // Firma y verifica tokens

    /**
     * Inicia sesión validando las credenciales del usuario
     * y devuelve un JWT válido si la autenticación fue exitosa.
     *
     * @param email Email ingresado
     * @param password Contraseña ingresada en texto plano
     * @return Token JWT con firma válida y duración de 24h
     */
    public String login(String email, String password) {
    	
    	// 1. Validar las credenciales (usuario y contraseña)
        Credencial cred = credencialService.validar(email, password);

        // 2. Verificación redundante por seguridad (hash contra el texto plano)
        if (cred == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // 3. Verificar si la contraseña es incorrecta usando el hash
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, cred.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // 4. Si todo es correcto, genera y devuelve el token JWT
        return jwtService.generateToken(cred.getUsuarioId(), cred.getRole());
    }


    /**
     * Registra una nueva credencial en el sistema, crea automáticamente
     * un usuario esqueleto, y devuelve un JWT para uso inmediato.
     *
     * @param dto Datos del registro (email + contraseña)
     * @return JWT firmado para el nuevo usuario
     */
    public String register(RegistroCredencialDTO dto) {

        // Internamente: guarda credencial + usuario + vincula usuarioId
        Credencial cred = credencialService.registrar(dto);

        log.debug("Usuario {} registrado con email {}", cred.getUsuarioId(), cred.getEmail());

        // JWT con sub = usuarioId
        return jwtService.generateToken(cred.getUsuarioId(), cred.getRole());
    }
}