package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payload consolidado do Dashboard Gerencial e Métricas Comerciais.
 * Mantém compatibilidade total com propriedades legadas do frontend (mesAtual, distribuicaoPagamentosHoje, topProdutosMes)
 * e novas propriedades por período (metricasPeriodo, distribuicaoPagamentos, topProdutos, lucratividade, divisaoPagamento, turnos).
 */
public record DashboardResumoOutput(
    @JsonProperty("hoje") MetricasFaturamentoDTO hoje,
    @JsonProperty("metricasPeriodo") MetricasFaturamentoDTO metricasPeriodo,
    @JsonProperty("mesAtual") MetricasFaturamentoDTO mesAtual,
    @JsonProperty("totalFiadoNaRua") BigDecimal totalFiadoNaRua,
    @JsonProperty("distribuicaoPagamentos") List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos,
    @JsonProperty("distribuicaoPagamentosHoje") List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje,
    @JsonProperty("topProdutos") List<TopProdutoVendidoDTO> topProdutos,
    @JsonProperty("topProdutosMes") List<TopProdutoVendidoDTO> topProdutosMes,
    @JsonProperty("lucratividade") LucroBrutoDTO lucratividade,
    @JsonProperty("lucratividadeHoje") LucroBrutoDTO lucratividadeHoje,
    @JsonProperty("divisaoPagamento") DivisaoPagamentoDTO divisaoPagamento,
    @JsonProperty("divisaoPagamentoHoje") DivisaoPagamentoDTO divisaoPagamentoHoje,
    @JsonProperty("turnos") TurnosVendaDTO turnos,
    @JsonProperty("turnosHoje") TurnosVendaDTO turnosHoje,
    @JsonProperty("lucroBruto") BigDecimal lucroBruto,
    @JsonProperty("margemPercentual") double margemPercentual,
    @JsonProperty("totalRecebidoAVista") BigDecimal totalRecebidoAVista,
    @JsonProperty("totalAFiado") BigDecimal totalAFiado,
    @JsonProperty("percentualFiado") double percentualFiado,
    @JsonProperty("totalAmortizado") BigDecimal totalAmortizado,
    @JsonProperty("turnoMaiorMovimento") String turnoMaiorMovimento
) {
    public DashboardResumoOutput(
        MetricasFaturamentoDTO hoje,
        MetricasFaturamentoDTO metricasPeriodo,
        BigDecimal totalFiadoNaRua,
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos,
        List<TopProdutoVendidoDTO> topProdutos,
        LucroBrutoDTO lucratividade,
        LucroBrutoDTO lucratividadeHoje,
        DivisaoPagamentoDTO divisaoPagamento,
        DivisaoPagamentoDTO divisaoPagamentoHoje,
        TurnosVendaDTO turnos,
        TurnosVendaDTO turnosHoje
    ) {
        this(
            hoje,
            metricasPeriodo,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            distribuicaoPagamentos,
            topProdutos,
            topProdutos,
            lucratividade,
            lucratividadeHoje,
            divisaoPagamento,
            divisaoPagamentoHoje,
            turnos,
            turnosHoje,
            lucratividade != null ? lucratividade.lucroBruto() : BigDecimal.ZERO,
            lucratividade != null ? lucratividade.margemPercentual() : 0.0,
            divisaoPagamento != null ? divisaoPagamento.totalRecebidoAVista() : BigDecimal.ZERO,
            divisaoPagamento != null ? divisaoPagamento.totalAFiado() : BigDecimal.ZERO,
            divisaoPagamento != null ? divisaoPagamento.percentualFiado() : 0.0,
            divisaoPagamento != null ? divisaoPagamento.totalAmortizado() : BigDecimal.ZERO,
            turnos != null ? turnos.turnoMaiorMovimento() : "-"
        );
    }

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
            totalFiadoNaRua,
            distribuicaoPagamentos,
            topProdutos,
            new LucroBrutoDTO(BigDecimal.ZERO, 0.0, BigDecimal.ZERO),
            new LucroBrutoDTO(BigDecimal.ZERO, 0.0, BigDecimal.ZERO),
            new DivisaoPagamentoDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0.0, 0.0, 0, 0),
            new DivisaoPagamentoDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0.0, 0.0, 0, 0),
            new TurnosVendaDTO(new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), "-"),
            new TurnosVendaDTO(new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), new TurnoDetalheDTO(BigDecimal.ZERO, 0), "-")
        );
    }
}
