package com.mercado.application.dto;

import com.mercado.domain.entity.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoDTO(
    UUID id,
    String nome,
    BigDecimal precoVenda,
    String unidade,
    BigDecimal estoqueAtual
) {
    public static ProdutoDTO from(Produto produto) {
        return new ProdutoDTO(
            produto.getId(),
            produto.getNome(),
            produto.getPrecoVenda().valor(),
            produto.getUnidade(),
            produto.getEstoqueAtual()
        );
    }
}
