package com.mercado.application.dto;

import com.mercado.domain.entity.Venda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO resumido para listagem de vendas do caixa atual.
 */
public record VendaResumoDTO(
    UUID id,
    BigDecimal valorTotal,
    String formaPagamento,
    String status,
    Instant criadoEm,
    String nomeCliente,
    int totalItens
) {
    public static VendaResumoDTO from(Venda venda) {
        return new VendaResumoDTO(
            venda.getId(),
            venda.getValorTotal().valor(),
            venda.getFormaPagamento().name(),
            venda.getStatus().name(),
            venda.getCriadoEm(),
            venda.getNomeCliente(),
            venda.getItens() != null ? venda.getItens().size() : 0
        );
    }
}
