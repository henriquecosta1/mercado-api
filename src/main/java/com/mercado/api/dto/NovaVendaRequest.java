package com.mercado.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload JSON recebido na criação de uma nova venda.
 * Aceita tanto 'nomeClienteFiado'/'telefoneClienteFiado' quanto 'nomeCliente'/'telefoneCliente',
 * além de lista opcional de itens do carrinho.
 */
public record NovaVendaRequest(
    BigDecimal valorTotal,
    BigDecimal valorRecebido,
    String formaPagamento,
    @JsonAlias({"nomeCliente", "nomeClienteFiado"})
    String nomeClienteFiado,
    @JsonAlias({"telefoneCliente", "telefoneClienteFiado"})
    String telefoneClienteFiado,
    String descricao,
    List<ItemVendaRequest> itens
) {
    public NovaVendaRequest(
        BigDecimal valorTotal,
        BigDecimal valorRecebido,
        String formaPagamento,
        String nomeClienteFiado,
        String telefoneClienteFiado,
        String descricao
    ) {
        this(valorTotal, valorRecebido, formaPagamento, nomeClienteFiado, telefoneClienteFiado, descricao, null);
    }
}
