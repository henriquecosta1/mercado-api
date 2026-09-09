package com.mercado.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Extrato completo e linha do tempo cronológica da conta de um cliente fiado.
 */
public record ExtratoClienteOutput(
    UUID clienteId,
    String nomeCliente,
    String telefone,
    BigDecimal saldoDevedorAtual,
    BigDecimal limiteCredito,
    List<TransacaoExtratoDTO> transacoes
) {
}
