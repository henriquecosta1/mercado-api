package com.mercado.application.dto;

import com.mercado.domain.entity.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saida com os dados cadastrais e financeiros completos do Cliente.
 */
public record ClienteDetalhadoDTO(
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
    public static ClienteDetalhadoDTO from(Cliente cliente) {
        return new ClienteDetalhadoDTO(
            cliente.getId(),
            cliente.getTenantId().valor(),
            cliente.getNome(),
            cliente.getApelido(),
            cliente.getTelefone(),
            cliente.getCpf(),
            cliente.getEndereco(),
            cliente.getPontoReferencia(),
            cliente.getLimiteCredito().valor(),
            cliente.getSaldoDevedor().valor(),
            cliente.getDiaVencimento(),
            cliente.getStatus().name(),
            cliente.getMotivoBloqueio(),
            cliente.getObservacoes(),
            cliente.getCriadoEm()
        );
    }
}