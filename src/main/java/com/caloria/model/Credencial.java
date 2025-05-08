package com.caloria.model;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "credenciales")
public class Credencial {
    @Id
    private String id;

    @Email
    private String email;

    private String passwordHash;

    private String role = "USER";

    /** Id del documento Usuario/Perfil asociado */
    private String usuarioId;
}