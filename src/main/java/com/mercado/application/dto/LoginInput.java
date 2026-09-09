package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para o caso de uso de autenticacao.
 *
 * @param login            login do usuario
 * @param senha            senha em texto puro
 * @param tenantIdOpcional tenant informado explicitamente (obrigatorio no login publico)
 */
public record LoginInput(
    String login,
    String senha,
    UUID tenantIdOpcional
) {
    public LoginInput(String login, String senha) {
        this(login, senha, null);
    }
}