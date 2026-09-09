package com.mercado.application.dto;

import com.mercado.domain.entity.CategoriaProduto;

import java.util.UUID;

/**
 * DTO representando uma categoria de produto na camada de aplicação.
 */
public record CategoriaDTO(
    UUID id,
    String nome,
    String icone,
    boolean ativo
) {
    public static CategoriaDTO from(CategoriaProduto categoria) {
        return new CategoriaDTO(
            categoria.getId(),
            categoria.getNome(),
            categoria.getIcone(),
            categoria.isAtivo()
        );
    }
}
