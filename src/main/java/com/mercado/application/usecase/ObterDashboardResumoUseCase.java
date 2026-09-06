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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Obter Métricas Comerciais e Resumo do Dashboard Gerencial.
 * Calcula consolidações financeiras do dia e do mês, total de fiado ativo e produtos curva ABC.
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
        return executar(tenantIdUuid, ZoneId.systemDefault(), Instant.now());
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, ZoneId zoneId, Instant agora) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para obter o resumo do dashboard.");
        }
        Objects.requireNonNull(zoneId, "ZoneId é obrigatório.");
        Objects.requireNonNull(agora, "Instant de referência é obrigatório.");

        TenantId tenantId = TenantId.de(tenantIdUuid);

        ZonedDateTime agoraZoned = agora.atZone(zoneId);
        Instant inicioHoje = agoraZoned.toLocalDate().atStartOfDay(zoneId).toInstant();
        Instant inicioMes = agoraZoned.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId).toInstant();

        // 1. Métricas de faturamento de hoje
        MetricasFaturamentoDTO hoje = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioHoje, agora);

        // 2. Métricas de faturamento do mês corrente
        MetricasFaturamentoDTO mesAtual = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioMes, agora);

        // 3. Saldo devedor total em aberto (Fiado na Rua)
        BigDecimal totalFiadoNaRua = clienteRepository.somarTotalSaldoDevedor(tenantId);

        // 4. Distribuição das vendas de hoje por forma de pagamento
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        // 5. Top 5 produtos mais vendidos no mês
        List<TopProdutoVendidoDTO> topProdutosMes = dashboardRepository.obterTopProdutos(
            tenantId,
            inicioMes,
            agora,
            5
        );

        return new DashboardResumoOutput(
            hoje,
            mesAtual,
            totalFiadoNaRua,
            distribuicaoPagamentosHoje,
            topProdutosMes
        );
    }
}
