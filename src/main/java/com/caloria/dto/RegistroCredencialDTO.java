package com.caloria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistroCredencialDTO {
    @Email
    private String email;

    @NotBlank
    private String password;
}