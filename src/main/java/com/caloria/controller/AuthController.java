package com.caloria.controller;

import com.caloria.service.AuthService;
import com.caloria.model.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        // Llamar al servicio de autenticación para validar las credenciales
        return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}