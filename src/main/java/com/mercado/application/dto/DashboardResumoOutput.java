package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload consolidado do Dashboard Gerencial e Métricas Comerciais.
 */
public record DashboardResumoOutput(
    MetricasFaturamentoDTO hoje,
    MetricasFaturamentoDTO mesAtual,
    BigDecimal totalFiadoNaRua,
    List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje,
    List<TopProdutoVendidoDTO> topProdutosMes
) {
}
