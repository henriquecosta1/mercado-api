package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Métricas analíticas de lucratividade, CMV e margem do período selecionado.
 */
public record LucroBrutoDTO(
    @JsonProperty("lucroBruto") BigDecimal lucroBruto,
    @JsonProperty("margemPercentual") double margemPercentual,
    @JsonProperty("faturamentoComCustoDefinido") BigDecimal faturamentoComCustoDefinido
) {
}
