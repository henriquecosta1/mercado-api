package com.mercado.application.dto;

import com.mercado.domain.entity.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que representa os dados públicos de um cliente na camada de aplicação.
 */
public record ClienteDTO(
    UUID id,
    UUID tenantId,
    String nome,
    String apelido,
    String telefone,
    String cpf,
    String endereco,
    String pontoReferencia,
    BigDecimal limiteCredito,
    BigDecimal saldoDevedor,
    Integer diaVencimento,
    String status,
    String motivoBloqueio,
    String observacoes,
    LocalDateTime criadoEm
) {
    public static ClienteDTO from(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return new ClienteDTO(
            cliente.getId(),
            cliente.getTenantId() != null ? cliente.getTenantId().valor() : null,
            cliente.getNome(),
            cliente.getApelido(),
            cliente.getTelefone(),
            cliente.getCpf(),
            cliente.getEndereco(),
            cliente.getPontoReferencia(),
            cliente.getLimiteCredito() != null ? cliente.getLimiteCredito().valor() : BigDecimal.ZERO,
            cliente.getSaldoDevedor() != null ? cliente.getSaldoDevedor().valor() : BigDecimal.ZERO,
            cliente.getDiaVencimento(),
            cliente.getStatus() != null ? cliente.getStatus().name() : null,
            cliente.getMotivoBloqueio(),
            cliente.getObservacoes(),
            cliente.getCriadoEm()
        );
    }
}
