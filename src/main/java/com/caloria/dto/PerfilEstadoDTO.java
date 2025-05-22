package com.caloria.dto;

import lombok.Value;
/**
 * DTO que resume el estado actual del proceso de configuración
 * del perfil del usuario.
 *
 * Se utiliza para mostrar en el frontend qué pasos han sido completados.
 */
@Value
public class PerfilEstadoDTO {
    boolean basicosCompletos;
    boolean actividadCompleta;
    boolean objetivoCompleto;
    boolean preferenciasCompletas;
    boolean perfilCompleto;
}