package com.mercado.application.dto;

import java.util.UUID;

/**
 * DTO de entrada para criação de uma categoria de produto.
 */
public record CriarCategoriaInput(
    UUID tenantId,
    String nome,
    String icone
) {
}
