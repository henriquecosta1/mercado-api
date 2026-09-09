package com.mercado.api.dto;

import java.util.UUID;

/**
 * Requisicao de login enviada pelo cliente.
 *
 * @param login    login do usuario
 * @param senha    senha em texto puro
 * @param tenantId UUID do tenant (obrigatorio no login publico)
 */
public record LoginRequest(
    String login,
    String senha,
    UUID tenantId
) {
}