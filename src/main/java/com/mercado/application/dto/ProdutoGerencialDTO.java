package com.mercado.application.dto;

import com.mercado.domain.entity.Produto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO com a visão gerencial completa de um produto e status de estoque.
 */
public record ProdutoGerencialDTO(
    UUID id,
    String nome,
    String categoria,
    BigDecimal precoVenda,
    BigDecimal precoCusto,
    BigDecimal precoPromocional,
    String unidade,
    BigDecimal estoqueAtual,
    BigDecimal estoqueMinimo,
    boolean ativo,
    boolean alertaEstoqueBaixo,
    String codigoBarras,
    String codigoInterno,
    boolean permiteFracionado
) {
    public ProdutoGerencialDTO(
        UUID id,
        String nome,
        String categoria,
        BigDecimal precoVenda,
        BigDecimal precoCusto,
        String unidade,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        boolean ativo,
        boolean alertaEstoqueBaixo
    ) {
        this(id, nome, categoria, precoVenda, precoCusto, null, unidade, estoqueAtual, estoqueMinimo, ativo, alertaEstoqueBaixo, null, null, false);
    }

    public static ProdutoGerencialDTO from(Produto produto) {
        return new ProdutoGerencialDTO(
            produto.getId(),
            produto.getNome(),
            produto.getCategoria(),
            produto.getPrecoVenda().valor(),
            produto.getPrecoCusto() != null ? produto.getPrecoCusto().valor() : null,
            produto.getPrecoPromocional() != null ? produto.getPrecoPromocional().valor() : null,
            produto.getUnidade(),
            produto.getEstoqueAtual(),
            produto.getEstoqueMinimo(),
            produto.isAtivo(),
            produto.isEstoqueAbaixoDoMinimo(),
            produto.getCodigoBarras(),
            produto.getCodigoInterno(),
            produto.isPermiteFracionado()
        );
    }
}
