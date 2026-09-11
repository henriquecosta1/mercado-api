package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item detalhado da venda contendo unidade de medida e subtotais.
 */
public record ItemVendaDetalheDTO(
    @JsonProperty("produtoId") UUID produtoId,
    @JsonProperty("nomeProduto") String nomeProduto,
    @JsonProperty("unidadeMedida") String unidadeMedida,
    @JsonProperty("quantidade") BigDecimal quantidade,
    @JsonProperty("precoUnitario") BigDecimal precoUnitario,
    @JsonProperty("subtotal") BigDecimal subtotal
) {
    @JsonProperty("descricao")
    public String descricao() {
        return nomeProduto;
    }
}
