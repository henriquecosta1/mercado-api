package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Dados resumidos do cliente associado à venda para a tela de Detalhes da Venda (F3).
 */
public record ClienteVendaDTO(
    @JsonProperty("id") UUID id,
    @JsonProperty("nome") String nome,
    @JsonProperty("telefone") String telefone
) {
}
