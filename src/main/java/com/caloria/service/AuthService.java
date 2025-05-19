package com.caloria.service;

import com.caloria.dto.RegistroCredencialDTO;
import com.caloria.model.Credencial;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j  
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredencialService credencialService;   // Maneja email + BCrypt
    private final JwtService jwtService;
    // Firma y verifica tokens

    /** Login: valida credenciales y devuelve JWT */
    public String login(String email, String password) {
        // Validar las credenciales (usuario y contraseña)
        Credencial cred = credencialService.validar(email, password);

        // Si no se encuentra el usuario
        if (cred == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Verificar si la contraseña es incorrecta usando el hash
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, cred.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Si todo es correcto, genera y devuelve el token JWT
        return jwtService.generateToken(cred.getUsuarioId(), cred.getRole());
    }


    /** Registro: crea credencial + usuario y devuelve JWT */
    public String register(RegistroCredencialDTO dto) {

        // CredencialService ya:
        //   • guarda credencial
        //   • crea Usuario esqueleto
        //   • asigna usuarioId
        Credencial cred = credencialService.registrar(dto);

        log.debug("Usuario {} registrado con email {}", cred.getUsuarioId(), cred.getEmail());

        // JWT con sub = usuarioId
        return jwtService.generateToken(cred.getUsuarioId(), cred.getRole());
    }
}