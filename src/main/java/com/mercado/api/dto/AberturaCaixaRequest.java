package com.mercado.api.dto;

import java.math.BigDecimal;

public record AberturaCaixaRequest(
    BigDecimal saldoInicial
) {}
