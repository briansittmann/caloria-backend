package com.caloria.model;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Entidad persistida en MongoDB que representa las credenciales de acceso
 * de un usuario: email, contraseña encriptada, rol de usuario y vínculo
 * con el documento Usuario correspondiente.
 *
 * Esta colección es independiente de la entidad Usuario para mejorar
 * la separación de responsabilidades (autenticación vs. perfil).
 */
@Data
@Document(collection = "credenciales")
public class Credencial {
	
    /**
     * ID único autogenerado por MongoDB.
     */
    @Id
    private String id;
    

    /**
     * Correo electrónico utilizado como identificador de login.
     * Validado como email y único en la base de datos.
     */
    @Email
    private String email;
    
    
    /**
     * Contraseña del usuario, almacenada como hash seguro (BCrypt).
     */
    private String passwordHash;
    
    
    /**
     * Rol del usuario, usado para autorización (por ejemplo: "USER", "ADMIN").
     */
    private String role = "USER";
    
    /**
     * Referencia al documento de la colección `usuarios` asociado a estas credenciales.
     * Este ID permite vincular la autenticación con el perfil de usuario.
     */
    private String usuarioId;
}