package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimentacaoInput(
    UUID tenantId,
    String tipo,
    BigDecimal valor,
    String motivo,
    String pin
) {
    public MovimentacaoInput(UUID tenantId, String tipo, BigDecimal valor, String motivo) {
        this(tenantId, tipo, valor, motivo, null);
    }
}
