package com.mercado.application.dto;

import com.mercado.domain.entity.StatusCliente;

import java.util.UUID;

/**
 * DTO de entrada para alteracao de status cadastral de cliente (ATIVO, BLOQUEADO, INATIVO).
 */
public record AlterarStatusClienteInput(
    UUID tenantId,
    UUID clienteId,
    StatusCliente status,
    String motivo
) {
}