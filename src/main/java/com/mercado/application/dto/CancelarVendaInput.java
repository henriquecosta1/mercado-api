package com.mercado.application.dto;

import java.util.UUID;

public record CancelarVendaInput(
    UUID tenantId,
    UUID vendaId,
    String motivo
) {}
