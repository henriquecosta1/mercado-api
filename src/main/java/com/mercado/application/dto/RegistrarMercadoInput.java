package com.mercado.application.dto;

/**
 * DTO de entrada para o fluxo de Onboarding de um novo Mercado.
 */
public record RegistrarMercadoInput(
    String nomeMercado,
    String nomeResponsavel,
    String login,
    String senha,
    String telefone,
    String pinGerente
) {
}
