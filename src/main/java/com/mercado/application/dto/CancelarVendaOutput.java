package com.mercado.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CancelarVendaOutput(
    UUID vendaId,
    String status,
    Instant canceladaEm
) {}
