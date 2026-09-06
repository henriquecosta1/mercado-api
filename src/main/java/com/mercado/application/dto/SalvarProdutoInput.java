package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para criação ou atualização de produto.
 */
public record SalvarProdutoInput(
    UUID tenantId,
    UUID id,
    String nome,
    String categoria,
    BigDecimal precoVenda,
    BigDecimal precoCusto,
    String unidade,
    BigDecimal estoqueInicial,
    BigDecimal estoqueMinimo
) {
}
