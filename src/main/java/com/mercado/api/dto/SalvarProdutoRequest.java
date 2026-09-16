package com.mercado.api.dto;

import java.math.BigDecimal;

/**
 * Payload JSON recebido na criação ou atualização completa de um produto.
 */
public record SalvarProdutoRequest(
    String nome,
    String categoria,
    BigDecimal precoVenda,
    BigDecimal precoCusto,
    BigDecimal precoPromocional,
    String unidade,
    BigDecimal estoqueInicial,
    BigDecimal estoqueMinimo,
    String codigoBarras,
    String codigoInterno,
    Boolean permiteFracionado
) {
    public SalvarProdutoRequest(
        String nome,
        String categoria,
        BigDecimal precoVenda,
        BigDecimal precoCusto,
        String unidade,
        BigDecimal estoqueInicial,
        BigDecimal estoqueMinimo
    ) {
        this(nome, categoria, precoVenda, precoCusto, null, unidade, estoqueInicial, estoqueMinimo, null, null, null);
    }
}
