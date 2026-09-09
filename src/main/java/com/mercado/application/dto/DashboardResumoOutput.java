package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payload consolidado do Dashboard Gerencial e Métricas Comerciais.
 * Mantém compatibilidade total com propriedades legadas do frontend (mesAtual, distribuicaoPagamentosHoje, topProdutosMes)
 * e novas propriedades por período (metricasPeriodo, distribuicaoPagamentos, topProdutos).
 */
public record DashboardResumoOutput(
    @JsonProperty("hoje") MetricasFaturamentoDTO hoje,
    @JsonProperty("metricasPeriodo") MetricasFaturamentoDTO metricasPeriodo,
    @JsonProperty("mesAtual") MetricasFaturamentoDTO mesAtual,
    @JsonProperty("totalFiadoNaRua") BigDecimal totalFiadoNaRua,
    @JsonProperty("distribuicaoPagamentos") List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos,
    @JsonProperty("distribuicaoPagamentosHoje") List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje,
    @JsonProperty("topProdutos") List<TopProdutoVendidoDTO> topProdutos,
    @JsonProperty("topProdutosMes") List<TopProdutoVendidoDTO> topProdutosMes
) {
    public DashboardResumoOutput(
        MetricasFaturamentoDTO hoje,
        MetricasFaturamentoDTO metricasPeriodo,
        BigDecimal totalFiadoNaRua,
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos,
        List<TopProdutoVendidoDTO> topProdutos
    ) {
        this(
            hoje,
            metricasPeriodo,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            distribuicaoPagamentos,
            topProdutos,
            topProdutos
        );
    }
}
