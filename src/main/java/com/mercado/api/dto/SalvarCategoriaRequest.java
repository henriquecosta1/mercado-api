package com.mercado.api.dto;

/**
 * Payload JSON recebido para criação e edição de categorias de produtos.
 */
public record SalvarCategoriaRequest(
    String nome,
    String icone
) {
}
