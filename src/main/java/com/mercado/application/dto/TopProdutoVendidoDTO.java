package com.mercado.application.dto;

import java.math.BigDecimal;

/**
 * DTO com dados agregados dos produtos mais vendidos no período.
 */
public record TopProdutoVendidoDTO(
    String nomeProduto,
    BigDecimal quantidadeTotal,
    BigDecimal subtotalTotal
) {
}
