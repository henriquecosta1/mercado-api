package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para bloqueio de cliente no fiado.
 */
public record BloquearClienteInput(
    UUID tenantId,
    UUID clienteId,
    String motivo
) {
}