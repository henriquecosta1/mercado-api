package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de saída com o resultado da venda registrada.
 */
public record RegistrarVendaOutput(
    UUID vendaId,
    BigDecimal valorTotal,
    BigDecimal troco,
    BigDecimal saldoDevedorCliente
) {}
