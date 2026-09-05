package com.mercado.api.dto;

import com.mercado.application.dto.RegistrarVendaOutput;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representação de resposta HTTP para a venda criada.
 */
public record VendaResponse(
    UUID vendaId,
    BigDecimal valorTotal,
    BigDecimal troco,
    BigDecimal saldoDevedorCliente
) {
    public static VendaResponse from(RegistrarVendaOutput output) {
        return new VendaResponse(
            output.vendaId(),
            output.valorTotal(),
            output.troco(),
            output.saldoDevedorCliente()
        );
    }
}
