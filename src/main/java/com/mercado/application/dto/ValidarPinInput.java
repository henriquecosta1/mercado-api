package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para validação do PIN de gerente.
 */
public record ValidarPinInput(
    UUID tenantId,
    String pin
) {
}
