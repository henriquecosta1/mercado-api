package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaInput(
    UUID produtoId,
    String descricao,
    BigDecimal quantidade,
    BigDecimal precoUnitario
) {}
