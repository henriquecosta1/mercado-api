package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercado.domain.entity.Produto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO com dados essenciais do produto para checkout rápido no PDV.
 */
public record ProdutoCarrinhoDTO(
    UUID id,
    String nome,
    @JsonProperty("descricao") String descricao,
    BigDecimal precoVenda,
    BigDecimal precoPromocional,
    String unidadeMedida,
    @JsonProperty("unidade") String unidade,
    BigDecimal estoqueAtual,
    String codigoBarras,
    String codigoInterno,
    boolean permiteFracionado
) {
    public static ProdutoCarrinhoDTO from(Produto produto) {
        return new ProdutoCarrinhoDTO(
            produto.getId(),
            produto.getNome(),
            produto.getNome(),
            produto.getPrecoVenda().valor(),
            produto.getPrecoPromocional() != null ? produto.getPrecoPromocional().valor() : null,
            produto.getUnidadeMedida(),
            produto.getUnidadeMedida(),
            produto.getEstoqueAtual(),
            produto.getCodigoBarras(),
            produto.getCodigoInterno(),
            produto.isPermiteFracionado()
        );
    }
}
