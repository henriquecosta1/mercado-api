package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para atualização de uma categoria de produto.
 */
public record AtualizarCategoriaInput(
    UUID tenantId,
    UUID id,
    String nome,
    String icone
) {
}
