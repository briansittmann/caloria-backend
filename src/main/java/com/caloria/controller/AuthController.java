package com.caloria.controller;

import com.caloria.dto.LoginRequestDTO;
import com.caloria.dto.LoginResponseDTO;
import com.caloria.dto.RegistroCredencialDTO;
import com.caloria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controlador REST responsable de la autenticación de usuarios:
 * login y registro. Devuelve un token JWT firmado si la operación es exitosa.
 *
 * Se comunica con {@link AuthService} para delegar la lógica de validación
 * y generación del token.
 *
 * Prefijo base: `/auth`
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	

    private final AuthService authService;
    
    
    /**
     * Inicia sesión para un usuario registrado.
     * 
     * Valida el email y contraseña ingresados y, si son correctos,
     * devuelve un token JWT válido por 24 horas.
     *
     * @param dto Datos de login: email y contraseña
     * @return Token JWT si es válido, o mensaje de error si falla la autenticación
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            String token = authService.login(dto.email(), dto.password());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (RuntimeException e) {
            // Captura los errores generados por el servicio de autenticación
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new LoginResponseDTO(e.getMessage()));  // El mensaje contiene el error
        }
    }
    
    
    /**
     * Registra una nueva credencial y un usuario esqueleto vinculado.
     *
     * Si el registro es exitoso, devuelve un JWT para usar directamente.
     *
     * @param dto Email y contraseña del nuevo usuario
     * @return Token JWT creado o mensaje de error en caso de fallo
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegistroCredencialDTO dto) {
        try {
            String token = authService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new LoginResponseDTO("Error en el registro: " + e.getMessage()));
        }
    }
}