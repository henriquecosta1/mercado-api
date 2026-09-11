package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO detalhado de resposta da Venda (Histórico / Tela F3).
 */
public record VendaDetalheResponseDTO(
    @JsonProperty("id") UUID id,
    @JsonProperty("dataHora") Instant dataHora,
    @JsonProperty("valorTotal") BigDecimal valorTotal,
    @JsonProperty("formaPagamento") String formaPagamento,
    @JsonProperty("status") String status,
    @JsonProperty("cliente") ClienteVendaDTO cliente,
    @JsonProperty("itens") List<ItemVendaDetalheDTO> itens
) {
    @JsonProperty("criadoEm")
    public Instant criadoEm() {
        return dataHora;
    }
}
