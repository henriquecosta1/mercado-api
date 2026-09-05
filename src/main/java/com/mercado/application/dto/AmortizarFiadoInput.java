package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para o caso de uso AmortizarFiadoUseCase.
 */
public record AmortizarFiadoInput(
    UUID tenantId,
    UUID clienteId,
    BigDecimal valorPago,
    String formaPagamento
) {}
