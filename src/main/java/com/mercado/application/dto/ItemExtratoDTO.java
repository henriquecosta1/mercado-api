package com.mercado.application.dto;

import java.math.BigDecimal;

/**
 * Item detalhado pertencente a uma compra do extrato do cliente.
 */
public record ItemExtratoDTO(
    String descricao,
    BigDecimal quantidade,
    BigDecimal subtotal
) {
}
