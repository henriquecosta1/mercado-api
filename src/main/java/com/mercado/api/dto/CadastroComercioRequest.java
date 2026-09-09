package com.mercado.api.dto;

/**
 * Payload da requisição de auto-cadastro de novos estabelecimentos (comércios).
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
