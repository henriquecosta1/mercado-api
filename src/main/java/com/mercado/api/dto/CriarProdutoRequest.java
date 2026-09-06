package com.mercado.api.dto;

import java.math.BigDecimal;

public record CriarProdutoRequest(
    String nome,
    BigDecimal precoVenda,
    String unidade,
    BigDecimal estoqueInicial
) {}
