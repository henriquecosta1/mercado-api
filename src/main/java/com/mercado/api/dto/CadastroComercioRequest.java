package com.mercado.api.dto;

/**
 * Payload da requisicao de auto-cadastro de novos estabelecimentos (comercios).
 */
public record CadastroComercioRequest(
    String nomeComercio,
    String whatsapp,
    String nomeGerente,
    String login,
    String senha,
    String pinGerente
) {
}