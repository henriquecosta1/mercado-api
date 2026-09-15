package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de saída para o caso de uso AmortizarFiadoUseCase.
 */
public record AmortizarFiadoOutput(
    UUID clienteId,
    BigDecimal valorPago,
    BigDecimal novoSaldoDevedor, BigDecimal troco
) {}
