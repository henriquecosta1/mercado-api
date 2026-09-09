package com.mercado.api.dto;

/**
 * Payload JSON recebido na requisição de cancelamento de uma venda.
 */
public record CancelarVendaRequest(
    String motivo,
    String pin,
    String pinGerente
) {
    public CancelarVendaRequest(String motivo) {
        this(motivo, null, null);
    }
}
