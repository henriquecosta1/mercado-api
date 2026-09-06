package com.mercado.api.dto;

/**
 * Payload JSON recebido na requisição de cancelamento de uma venda.
 */
public record CancelarVendaRequest(
    String motivo
) {
}
