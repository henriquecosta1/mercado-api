package com.mercado.api.dto;

/**
 * Payload JSON recebido no endpoint publico de Onboarding de Mercado.
 */
public record RegistrarMercadoRequest(
    String nomeMercado,
    String nomeResponsavel,
    String login,
    String senha,
    String telefone,
    String pinGerente
) {
}