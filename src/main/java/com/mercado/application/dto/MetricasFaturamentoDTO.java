package com.mercado.application.dto;

import java.math.BigDecimal;

/**
 * DTO contendo métricas agregadas de faturamento e volume de vendas.
 */
public record MetricasFaturamentoDTO(
    BigDecimal faturamentoTotal,
    int totalVendas,
    BigDecimal ticketMedio
) {
}
