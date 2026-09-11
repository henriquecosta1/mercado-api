package com.mercado.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agrupamento de vendas por faixas horárias / turnos (Manhã, Tarde, Noite, Madrugada) e turno pico.
 */
public record TurnosVendaDTO(
    @JsonProperty("manha") TurnoDetalheDTO manha,
    @JsonProperty("tarde") TurnoDetalheDTO tarde,
    @JsonProperty("noite") TurnoDetalheDTO noite,
    @JsonProperty("madrugada") TurnoDetalheDTO madrugada,
    @JsonProperty("turnoMaiorMovimento") String turnoMaiorMovimento
) {
}
