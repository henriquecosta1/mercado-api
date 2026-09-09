package com.mercado.application.dto;

/**
 * DTO de entrada para auto-cadastro de novos estabelecimentos (comércios/lojas).
 */
public record CadastroComercioInput(
    String nomeComercio,
    String whatsapp,
    String nomeGerente,
    String login,
    String senha,
    String pinGerenteOpcional
) {
    public CadastroComercioInput(String nomeComercio, String whatsapp, String nomeGerente, String login, String senha) {
        this(nomeComercio, whatsapp, nomeGerente, login, senha, null);
    }
}
