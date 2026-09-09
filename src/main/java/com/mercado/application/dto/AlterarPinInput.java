package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para alteração do PIN de gerente.
 */
public record AlterarPinInput(
    UUID tenantId,
    String pinAtual,
    String novoPin
) {
}
