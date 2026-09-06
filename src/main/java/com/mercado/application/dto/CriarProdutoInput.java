package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarProdutoInput(
    UUID tenantId,
    String nome,
    BigDecimal precoVenda,
    String unidade,
    BigDecimal estoqueInicial
) {}
