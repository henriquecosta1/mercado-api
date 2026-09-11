package com.mercado.application.repository;

import com.mercado.application.dto.DivisaoPagamentoDTO;
import com.mercado.application.dto.LucroBrutoDTO;
import com.mercado.application.dto.MetricasFaturamentoDTO;
import com.mercado.application.dto.TopProdutoVendidoDTO;
import com.mercado.application.dto.TotalPorFormaPagamentoDTO;
import com.mercado.application.dto.TurnosVendaDTO;
import com.mercado.domain.valueobject.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Porta de saída (Port) para consultas analíticas e agregadas do Dashboard.
 * Princípio da Inversão de Dependência (DIP) da Clean Architecture.
 */
public interface DashboardRepository {

    MetricasFaturamentoDTO calcularMetricasFaturamento(TenantId tenantId, Instant de, Instant ate);

    List<TotalPorFormaPagamentoDTO> obterDistribuicaoPagamentos(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal);

    List<TopProdutoVendidoDTO> obterTopProdutos(TenantId tenantId, Instant de, Instant ate, int limite);

    LucroBrutoDTO calcularLucroBruto(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal);

    DivisaoPagamentoDTO calcularDivisaoPagamento(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal);

    TurnosVendaDTO calcularTurnosVenda(TenantId tenantId, Instant de, Instant ate, ZoneId zoneId);
}
