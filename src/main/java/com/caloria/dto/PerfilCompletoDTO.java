package com.caloria.dto;

import lombok.Value;

/**
 * DTO que indica si el perfil del usuario ha sido completamente configurado.
 * Se utiliza para mostrar el estado general del progreso en el onboarding.
 */
@Value
public class PerfilCompletoDTO {
    boolean perfilCompleto;
}