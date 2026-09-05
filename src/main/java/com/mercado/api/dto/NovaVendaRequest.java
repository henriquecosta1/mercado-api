package com.mercado.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

/**
 * Payload JSON recebido na criação de uma nova venda.
 * Aceita tanto 'nomeClienteFiado'/'telefoneClienteFiado' quanto 'nomeCliente'/'telefoneCliente'.
 */
public record NovaVendaRequest(
    BigDecimal valorTotal,
    BigDecimal valorRecebido,
    String formaPagamento,
    @JsonAlias({"nomeCliente", "nomeClienteFiado"})
    String nomeClienteFiado,
    @JsonAlias({"telefoneCliente", "telefoneClienteFiado"})
    String telefoneClienteFiado,
    String descricao
) {}
