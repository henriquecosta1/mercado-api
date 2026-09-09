package com.mercado.application.dto;

import java.util.UUID;

public record CancelarVendaInput(
    UUID tenantId,
    UUID vendaId,
    String motivo,
    String pin
) {
    public CancelarVendaInput(UUID tenantId, UUID vendaId, String motivo) {
        this(tenantId, vendaId, motivo, null);
    }
}
