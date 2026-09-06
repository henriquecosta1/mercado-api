package com.mercado.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResumoCaixaOutput(
    UUID caixaId,
    String status,
    Instant abertoEm,
    BigDecimal saldoInicial,
    BigDecimal saldoDinheiroAtual,
    BigDecimal totalVendasDinheiro,
    BigDecimal totalVendasPix,
    BigDecimal totalVendasCartao,
    BigDecimal totalVendasFiado,
    BigDecimal totalSangrias,
    BigDecimal totalSuprimentos,
    int quantidadeVendas
) {}
