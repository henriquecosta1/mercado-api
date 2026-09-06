package com.mercado.application.dto;

import java.math.BigDecimal;

/**
 * DTO contendo a distribuição financeira e quantitativa por forma de pagamento.
 */
public record TotalPorFormaPagamentoDTO(
    String formaPagamento,
    BigDecimal total,
    int quantidade,
    double percentual
) {
}
