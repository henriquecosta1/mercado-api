package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de saída do auto-cadastro de estabelecimento.
 */
public record CadastroComercioOutput(
    UUID tenantId,
    String nomeComercio,
    String status,
    String mensagem
) {
}
