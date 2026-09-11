package com.mercado.application.usecase;

import com.mercado.application.dto.DashboardResumoOutput;
import com.mercado.application.dto.DivisaoPagamentoDTO;
import com.mercado.application.dto.LucroBrutoDTO;
import com.mercado.application.dto.MetricasFaturamentoDTO;
import com.mercado.application.dto.TopProdutoVendidoDTO;
import com.mercado.application.dto.TotalPorFormaPagamentoDTO;
import com.mercado.application.dto.TurnosVendaDTO;
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
 * Caso de Uso: Obter MÃ©tricas Comerciais e Resumo do Dashboard Gerencial.
 * Calcula consolidaÃ§Ãµes financeiras do dia e do perÃ­odo selecionado (HOJE, 7DIAS, MES, PERSONALIZADO),
 * total de fiado ativo, ranking de produtos mais vendidos, lucratividade/margem, divisÃ£o Ã  vista vs. fiado e turnos.
 */
@ApplicationScoped
public class ObterDashboardResumoUseCase {

    private final DashboardRepository dashboardRepository;
    private final ClienteRepository clienteRepository;

    @Inject
    public ObterDashboardResumoUseCase(DashboardRepository dashboardRepository,
                                       ClienteRepository clienteRepository) {
        this.dashboardRepository = Objects.requireNonNull(dashboardRepository, "DashboardRepository Ã© obrigatÃ³rio.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository Ã© obrigatÃ³rio.");
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
            throw new IllegalArgumentException("TenantId Ã© obrigatÃ³rio para obter o resumo do dashboard.");
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

        // 1. MÃ©tricas de faturamento de hoje (inÃ­cio do dia atÃ© agora)
        MetricasFaturamentoDTO hoje = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioHoje, agora);

        // 2. MÃ©tricas de faturamento do perÃ­odo selecionado
        MetricasFaturamentoDTO metricasPeriodo = dashboardRepository.calcularMetricasFaturamento(tenantId, inicioPeriodo, fimPeriodo);

        // 3. Saldo devedor total em aberto (Fiado na Rua)
        BigDecimal totalFiadoNaRua = clienteRepository.somarTotalSaldoDevedor(tenantId);

        // 4. DistribuiÃ§Ã£o das vendas no perÃ­odo selecionado e hoje por forma de pagamento
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentos = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            metricasPeriodo.faturamentoTotal()
        );
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        // 5. Top 5 produtos mais vendidos no perÃ­odo selecionado
        List<TopProdutoVendidoDTO> topProdutos = dashboardRepository.obterTopProdutos(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            5
        );

        // 6. Lucratividade do perÃ­odo e de hoje
        LucroBrutoDTO lucratividadePeriodo = dashboardRepository.calcularLucroBruto(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            metricasPeriodo.faturamentoTotal()
        );
        LucroBrutoDTO lucratividadeHoje = dashboardRepository.calcularLucroBruto(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        // 7. DivisÃ£o de Pagamento (Ã€ Vista vs. Fiado)
        DivisaoPagamentoDTO divisaoPeriodo = dashboardRepository.calcularDivisaoPagamento(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            metricasPeriodo.faturamentoTotal()
        );
        DivisaoPagamentoDTO divisaoHoje = dashboardRepository.calcularDivisaoPagamento(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        // 8. HorÃ¡rios de Maior Movimento (Turnos)
        TurnosVendaDTO turnosPeriodo = dashboardRepository.calcularTurnosVenda(
            tenantId,
            inicioPeriodo,
            fimPeriodo,
            zone
        );
        TurnosVendaDTO turnosHoje = dashboardRepository.calcularTurnosVenda(
            tenantId,
            inicioHoje,
            agora,
            zone
        );

        return new DashboardResumoOutput(
            hoje,
            metricasPeriodo,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            distribuicaoPagamentosHoje,
            topProdutos,
            topProdutos,
            lucratividadePeriodo,
            lucratividadeHoje,
            divisaoPeriodo,
            divisaoHoje,
            turnosPeriodo,
            turnosHoje,
            lucratividadePeriodo.lucroBruto(),
            lucratividadePeriodo.margemPercentual(),
            divisaoPeriodo.totalRecebidoAVista(),
            divisaoPeriodo.totalAFiado(),
            divisaoPeriodo.percentualFiado(),
            divisaoPeriodo.totalAmortizado(),
            turnosPeriodo.turnoMaiorMovimento()
        );
    }

    public DashboardResumoOutput executar(UUID tenantIdUuid, ZoneId zoneId, Instant agora) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId Ã© obrigatÃ³rio para obter o resumo do dashboard.");
        }
        Objects.requireNonNull(zoneId, "ZoneId Ã© obrigatÃ³rio.");
        Objects.requireNonNull(agora, "Instant de referÃªncia Ã© obrigatÃ³rio.");

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
        List<TotalPorFormaPagamentoDTO> distribuicaoPagamentosHoje = dashboardRepository.obterDistribuicaoPagamentos(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        List<TopProdutoVendidoDTO> topProdutos = dashboardRepository.obterTopProdutos(
            tenantId,
            inicioMes,
            agora,
            5
        );

        LucroBrutoDTO lucratividadePeriodo = dashboardRepository.calcularLucroBruto(
            tenantId,
            inicioMes,
            agora,
            metricasPeriodo.faturamentoTotal()
        );
        LucroBrutoDTO lucratividadeHoje = dashboardRepository.calcularLucroBruto(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        DivisaoPagamentoDTO divisaoPeriodo = dashboardRepository.calcularDivisaoPagamento(
            tenantId,
            inicioMes,
            agora,
            metricasPeriodo.faturamentoTotal()
        );
        DivisaoPagamentoDTO divisaoHoje = dashboardRepository.calcularDivisaoPagamento(
            tenantId,
            inicioHoje,
            agora,
            hoje.faturamentoTotal()
        );

        TurnosVendaDTO turnosPeriodo = dashboardRepository.calcularTurnosVenda(
            tenantId,
            inicioMes,
            agora,
            zoneId
        );
        TurnosVendaDTO turnosHoje = dashboardRepository.calcularTurnosVenda(
            tenantId,
            inicioHoje,
            agora,
            zoneId
        );

        return new DashboardResumoOutput(
            hoje,
            metricasPeriodo,
            metricasPeriodo,
            totalFiadoNaRua,
            distribuicaoPagamentos,
            distribuicaoPagamentosHoje,
            topProdutos,
            topProdutos,
            lucratividadePeriodo,
            lucratividadeHoje,
            divisaoPeriodo,
            divisaoHoje,
            turnosPeriodo,
            turnosHoje,
            lucratividadePeriodo.lucroBruto(),
            lucratividadePeriodo.margemPercentual(),
            divisaoPeriodo.totalRecebidoAVista(),
            divisaoPeriodo.totalAFiado(),
            divisaoPeriodo.percentualFiado(),
            divisaoPeriodo.totalAmortizado(),
            turnosPeriodo.turnoMaiorMovimento()
        );
    }
}

