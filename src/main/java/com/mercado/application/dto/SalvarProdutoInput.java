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
    BigDecimal precoPromocional,
    String unidade,
    BigDecimal estoqueInicial,
    BigDecimal estoqueMinimo,
    String codigoBarras,
    String codigoInterno,
    Boolean permiteFracionado
) {
    public SalvarProdutoInput(
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
        this(tenantId, id, nome, categoria, precoVenda, precoCusto, null, unidade, estoqueInicial, estoqueMinimo, null, null, null);
    }
}
