package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Métricas analíticas de divisão de faturamento: À Vista vs. Fiado.
 * Considera amortizações (abatimentos de fiado recebidos no período)
 * para calcular o recebimento real e o fiado pendente em aberto.
 */
public record DivisaoPagamentoDTO(
    @JsonProperty("totalRecebidoAVista") BigDecimal totalRecebidoAVista,
    @JsonProperty("totalAFiado") BigDecimal totalAFiado,
    @JsonProperty("percentualFiado") double percentualFiado,
    @JsonProperty("percentualAVista") double percentualAVista,
    @JsonProperty("totalVendasAVista") int totalVendasAVista,
    @JsonProperty("totalVendasFiado") int totalVendasFiado,
    @JsonProperty("totalAmortizado") BigDecimal totalAmortizado
) {
    public DivisaoPagamentoDTO(
        BigDecimal totalRecebidoAVista,
        BigDecimal totalAFiado,
        double percentualFiado,
        double percentualAVista,
        int totalVendasAVista,
        int totalVendasFiado
    ) {
        this(totalRecebidoAVista, totalAFiado, percentualFiado, percentualAVista, totalVendasAVista, totalVendasFiado, BigDecimal.ZERO);
    }
}
