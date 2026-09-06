package com.mercado.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FecharCaixaOutput(
    UUID caixaId,
    BigDecimal saldoDinheiroFinal,
    Instant fechadoEm
) {}
