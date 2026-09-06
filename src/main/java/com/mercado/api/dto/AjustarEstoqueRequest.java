package com.mercado.api.dto;

import java.math.BigDecimal;

/**
 * Payload JSON para ajuste manual de estoque físico.
 */
public record AjustarEstoqueRequest(
    BigDecimal novoEstoque,
    String motivo
) {
}
