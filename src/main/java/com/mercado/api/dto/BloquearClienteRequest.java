package com.mercado.api.dto;

/**
 * Requisicao HTTP para alteracao de status / bloqueio de cliente.
 * status pode ser: ATIVO, BLOQUEADO, INATIVO.
 */
public record BloquearClienteRequest(
    String status,
    String motivo
) {
}