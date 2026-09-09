package com.mercado.application.dto;

/**
 * DTO de entrada para ativação/renovação de licença de um tenant.
 */
public record AtivarTenantInput(
    int diasValidade
) {
}
