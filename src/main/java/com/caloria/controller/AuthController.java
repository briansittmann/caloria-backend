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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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