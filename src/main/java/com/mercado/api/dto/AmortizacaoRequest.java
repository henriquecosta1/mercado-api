package com.mercado.api.dto;

import java.math.BigDecimal;

/**
 * Payload JSON recebido na amortização de débito de fiado.
 */
public record AmortizacaoRequest(
    BigDecimal valorPago,
    String formaPagamento
) {}
