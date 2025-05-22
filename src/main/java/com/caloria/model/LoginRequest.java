package com.caloria.model;
import lombok.Data;

/**
 * Objeto utilizado para encapsular los datos enviados durante el inicio de sesión.
 *
 * Este DTO es procesado por el endpoint de login, que valida las credenciales
 * y genera un token JWT si son correctas.
 */
@Data 
public class LoginRequest {
    
	/** Dirección de correo electrónico del usuario */
    private String email;
    
    /** Contraseña del usuario (en texto plano) */
    private String password;

}