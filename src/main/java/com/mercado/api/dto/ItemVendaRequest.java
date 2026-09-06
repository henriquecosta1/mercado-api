package com.mercado.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaRequest(
    UUID produtoId,
    String descricao,
    BigDecimal quantidade,
    BigDecimal precoUnitario
) {}
