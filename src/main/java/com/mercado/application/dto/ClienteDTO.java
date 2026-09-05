package com.mercado.application.dto;

import com.mercado.domain.entity.Cliente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO que representa os dados públicos de um cliente na camada de aplicação.
 */
public record ClienteDTO(
    UUID id,
    String nome,
    String telefone,
    BigDecimal limiteCredito,
    BigDecimal saldoDevedor
) {
    public static ClienteDTO from(Cliente cliente) {
        return new ClienteDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getTelefone(),
            cliente.getLimiteCredito().valor(),
            cliente.getSaldoDevedor().valor()
        );
    }
}
