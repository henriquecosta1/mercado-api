package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para ajuste manual do estoque físico.
 */
public record AjustarEstoqueInput(
    UUID tenantId,
    UUID produtoId,
    BigDecimal novoEstoque,
    String motivo
) {
}
