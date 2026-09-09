package com.mercado.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representação unificada de uma movimentação (débito por COMPRA ou crédito por PAGAMENTO) na conta corrente do cliente.
 */
public record TransacaoExtratoDTO(
    UUID id,
    String tipo,
    Instant dataHora,
    BigDecimal valor,
    String formaPagamento,
    List<ItemExtratoDTO> itens
) {
}
