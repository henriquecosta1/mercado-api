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
    String unidade,
    BigDecimal estoqueInicial,
    BigDecimal estoqueMinimo
) {
}
