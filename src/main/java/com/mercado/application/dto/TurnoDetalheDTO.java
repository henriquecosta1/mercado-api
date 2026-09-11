package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Faturamento e volume de vendas de um turno específico.
 */
public record TurnoDetalheDTO(
    @JsonProperty("faturamento") BigDecimal faturamento,
    @JsonProperty("totalVendas") int totalVendas
) {
}
