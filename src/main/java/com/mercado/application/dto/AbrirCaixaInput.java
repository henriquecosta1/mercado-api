package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AbrirCaixaInput(
    UUID tenantId,
    BigDecimal saldoInicial
) {}
