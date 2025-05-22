package com.caloria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * DTO utilizado durante el registro de un nuevo usuario.
 *
 * Contiene los datos mínimos necesarios para crear una credencial segura:
 * - Email con formato válido.
 * - Contraseña no vacía (texto plano).
 *
 * Este DTO es consumido por el endpoint de registro y validado automáticamente
 * mediante anotaciones de Jakarta Bean Validation.
 */
@Data
public class RegistroCredencialDTO {
	
    /**
     * Dirección de correo electrónico del usuario.
     * Debe tener un formato válido y único en el sistema.
     */
    @Email
    private String email;
    
    
    /**
     * Contraseña del usuario (en texto plano).
     * Será encriptada antes de almacenarse.
     */
    @NotBlank
    private String password;
}