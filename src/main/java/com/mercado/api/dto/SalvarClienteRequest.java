package com.mercado.api.dto;

import java.math.BigDecimal;

/**
 * Requisicao HTTP para cadastrar ou atualizar cliente completo.
 */
public record SalvarClienteRequest(
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