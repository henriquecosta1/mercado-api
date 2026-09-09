package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para criacao ou atualizacao completa de Cliente.
 */
public record SalvarClienteInput(
    UUID tenantId,
    UUID id,
    String nome,
    String apelido,
    String telefone,
    String cpf,
    String endereco,
    String pontoReferencia,
    BigDecimal limiteCredito,
    Integer diaVencimento,
    String observacoes
) {
}