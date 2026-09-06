package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de entrada para o caso de uso RegistrarVendaUseCase.
 */
public record RegistrarVendaInput(
    UUID tenantId,
    BigDecimal valorTotal,
    BigDecimal valorRecebido,
    String formaPagamento,
    String nomeClienteFiado,
    String telefoneClienteFiado,
    String descricao,
    List<ItemVendaInput> itens
) {
    public RegistrarVendaInput(
        UUID tenantId,
        BigDecimal valorTotal,
        BigDecimal valorRecebido,
        String formaPagamento,
        String nomeClienteFiado,
        String telefoneClienteFiado,
        String descricao
    ) {
        this(tenantId, valorTotal, valorRecebido, formaPagamento, nomeClienteFiado, telefoneClienteFiado, descricao, null);
    }
}
