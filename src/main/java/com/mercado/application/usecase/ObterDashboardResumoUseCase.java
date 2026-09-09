package com.mercado.application.usecase;

import com.mercado.application.dto.DashboardResumoOutput;
import com.mercado.application.dto.MetricasFaturamentoDTO;
import com.mercado.application.dto.TopProdutoVendidoDTO;
import com.mercado.application.dto.TotalPorFormaPagamentoDTO;
import com.mercado.application.repository.DashboardRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Obter Métricas Comerciais e Resumo do Dashboard Gerencial.
 * Calcula consolidações financeiras do dia e do período selecionado (HOJE, 7DIAS, MES, PERSONALIZADO),
 * total de fiado ativo e ranking de produtos mais vendidos.
 */
@ApplicationScoped
public class ObterDashboardResumoUseCase {

    private final DashboardRepository dashboardRepository;
    private final ClienteRepository clienteRepository;

    @Inject
    public ObterDashboardResumoUseCase(DashboardRepository dashboardRepository,
                                       ClienteRepository clienteRepository) {
        this.dashboardRepository = Objects.requireNonNull(dashboardRepository, "DashboardRepository é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid) {
        return executar(tenantIdUuid, null, null, null, ZoneId.systemDefault());
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, LocalDate dataInicio, LocalDate dataFim) {
        return executar(tenantIdUuid, null, dataInicio, dataFim, ZoneId.systemDefault());
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, LocalDate dataInicio, LocalDate dataFim, ZoneId zoneId) {
        return executar(tenantIdUuid, null, dataInicio, dataFim, zoneId);
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, String periodo, LocalDate dataInicio, LocalDate dataFim, ZoneId zoneId) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para obter o resumo do dashboard.");
        }
        ZoneId zone = (zoneId != null) ? zoneId : ZoneId.systemDefault();
        TenantId tenantId = TenantId.de(tenantIdUuid);

        LocalDate hojeLocal = LocalDate.now(zone);
        LocalDate inicioEfetivo;
        LocalDate fimEfetivo;

        if (dataInicio != null || dataFim != null) {
            inicioEfetivo = (dataInicio != null) ? dataInicio : hojeLocal.withDayOfMonth(1);
            fimEfetivo = (dataFim != null) ? dataFim : hojeLocal;
        } else if (periodo != null && !periodo.isBlank()) {
            switch (periodo.trim().toUpperCase()) {
                case "HOJE" -> {
                    inicioEfetivo = hojeLocal;
                    fimEfetivo = hojeLocal;
                }
                case "7DIAS", "SETE_DIAS", "ULTIMOS_7_DIAS" -> {
                    inicioEfetivo = hojeLocal.minusDays(7);
                    fimEfetivo = hojeLocal;
                }
                case "MES", "ESTE_MES", "MES_ATUAL" -> {
                    inicioEfetivo = hojeLocal.withDayOfMonth(1);
                    fimEfetivo = hojeLocal;
                }
                default -> {
                    inicioEfetivo = hojeLocal.withDayOfMonth(1);
                    fimEfetivo = hojeLocal;
                }
            }
        } else {
            inicioEfetivo = hojeLocal.withDayOfMonth(1);
            fimEfetivo = hojeLocal;
        }

        Instant inicioPeriodo = inicioEfetivo.atStartOfDay(zone).toInstant();
        Instant fimPeriodo = fimEfetivo.atTime(23, 59, 59, 999_999_999).atZone(zone).toInstant();

        Instant inicioHoje = hojeLocal.atStartOfDay(zone).toInstant();
        Instant agora = Instant.now();

        // 1. Métricas de faturamento de hoje (início do dia até agora)
        MetricasFaturamentoDTO hoje = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioHoje, agora);

        // 2. Métricas de faturamento do período selecionado
        MetricasFaturamentoDTO metricasPeriodo = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioPeriodo, fimPeriodo);

        // 3. Saldo devedor total em aberto (Fiado na Rua)
        BigDecimal totalFiadoNaRua = clienteRepository.somarTotalSaldoDevedor(tenantId);

        // 4. Distribuição das vendas no período selecionado por forma de pagamento
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            metricasPeriodo.faturamentoTotal()
        );

        // 5. Top 5 produtos mais vendidos no período selecionado
        List<TopProdutoVendidoDTO> topProdutos = dashboardRepository.obterTopProdutos(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            5
        );

        return new DashboardResumoOutput(
            hoje,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            topProdutos
        );
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, ZoneId zoneId, Instant agora) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para obter o resumo do dashboard.");
        }
        Objects.requireNonNull(zoneId, "ZoneId é obrigatório.");
        Objects.requireNonNull(agora, "Instant de referência é obrigatório.");

        TenantId tenantId = TenantId.de(tenantIdUuid);

        ZonedDateTime agoraZoned = agora.atZone(zoneId);
        LocalDate hojeLocal = agoraZoned.toLocalDate();
        Instant inicioHoje = hojeLocal.atStartOfDay(zoneId).toInstant();
        Instant inicioMes = agoraZoned.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId).toInstant();

        MetricasFaturamentoDTO hoje = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioHoje, agora);
        MetricasFaturamentoDTO metricasPeriodo = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioMes, agora);
        BigDecimal totalFiadoNaRua = clienteRepository.somarTotalSaldoDevedor(tenantId);

        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioMes,
            agora,
            metricasPeriodo.faturamentoTotal()
        );

        List<TopProdutoVendidoDTO> topProdutos = dashboardRepository.obterTopProdutos(
            tenantId,
            inicioMes,
            agora,
            5
        );

        return new DashboardResumoOutput(
            hoje,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            topProdutos
        );
    }
}
