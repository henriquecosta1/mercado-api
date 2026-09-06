package com.mercado.api.dto;

import java.math.BigDecimal;

public record MovimentacaoRequest(
    String tipo,
    BigDecimal valor,
    String motivo
) {}
