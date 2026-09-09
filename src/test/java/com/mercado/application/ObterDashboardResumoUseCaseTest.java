package com.mercado.application;

import com.mercado.application.dto.*;
import com.mercado.application.repository.DashboardRepository;
import com.mercado.application.usecase.ObterDashboardResumoUseCase;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: ObterDashboardResumoUseCase")
class ObterDashboardResumoUseCaseTest {

    private FakeDashboardRepository dashboardRepository;
    private FakeClienteRepository clienteRepository;
    private ObterDashboardResumoUseCase useCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        dashboardRepository = new FakeDashboardRepository();
        clienteRepository = new FakeClienteRepository();
        useCase = new ObterDashboardResumoUseCase(dashboardRepository, clienteRepository);
    }

    @Test
    @DisplayName("Deve calcular resumo do dashboard com métricas de hoje, mês, fiado na rua e top produtos")
    void deveCalcularDashboardResumoComSucesso() {
        // Prepara fiado na rua (2 clientes com débito)
        Cliente c1 = Cliente.criar(tenantId, "João", "11999990001", Dinheiro.zero());
        c1.registrarDebito(Dinheiro.de("150.00"));
        clienteRepository.salvar(c1);

        Cliente c2 = Cliente.criar(tenantId, "Maria", "11999990002", Dinheiro.zero());
        c2.registrarDebito(Dinheiro.de("200.00"));
        clienteRepository.salvar(c2);

        // Configura mocks no FakeDashboardRepository
        dashboardRepository.metricasHoje = new MetricasFaturamentoDTO(
            new BigDecimal("300.00"),
            3,
            new BigDecimal("100.00")
        );
        dashboardRepository.metricasMes = new MetricasFaturamentoDTO(
            new BigDecimal("2500.00"),
            25,
            new BigDecimal("100.00")
        );
        dashboardRepository.distribuicaoHoje = List.of(
            new TotalPorFormaPagamentoDTO("DINHEIRO", new BigDecimal("200.00"), 2, 66.67),
            new TotalPorFormaPagamentoDTO("PIX", new BigDecimal("100.00"), 1, 33.33)
        );
        dashboardRepository.topProdutos = List.of(
            new TopProdutoVendidoDTO("Arroz 5kg", new BigDecimal("10.000"), new BigDecimal("250.00")),
            new TopProdutoVendidoDTO("Café 500g", new BigDecimal("8.000"), new BigDecimal("144.00"))
        );

        Instant agora = Instant.parse("2026-09-06T15:00:00Z");
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");

        DashboardResumoOutput output = useCase.executar(tenantIdRaw, zoneId, agora);

        assertNotNull(output);

        // Valida Hoje
        assertEquals(new BigDecimal("300.00"), output.hoje().faturamentoTotal());
        assertEquals(3, output.hoje().totalVendas());
        assertEquals(new BigDecimal("100.00"), output.hoje().ticketMedio());

        // Valida Mês
        assertEquals(new BigDecimal("2500.00"), output.mesAtual().faturamentoTotal());
        assertEquals(25, output.mesAtual().totalVendas());

        // Valida Fiado na rua (150 + 200 = 350.00)
        assertEquals(new BigDecimal("350.00"), output.totalFiadoNaRua());

        // Valida Distribuição de pagamentos
        assertEquals(2, output.distribuicaoPagamentosHoje().size());
        assertEquals("DINHEIRO", output.distribuicaoPagamentosHoje().get(0).formaPagamento());
        assertEquals(66.67, output.distribuicaoPagamentosHoje().get(0).percentual());

        // Valida Top produtos
        assertEquals(2, output.topProdutosMes().size());
        assertEquals("Arroz 5kg", output.topProdutosMes().get(0).nomeProduto());
    }

    @Test
    @DisplayName("Deve retornar métricas zeradas quando não houver vendas ou fiado")
    void deveRetornarMetricasZeradasQuandoNaoHouverVendas() {
        dashboardRepository.metricasHoje = new MetricasFaturamentoDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO);
        dashboardRepository.metricasMes = new MetricasFaturamentoDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO);
        dashboardRepository.distribuicaoHoje = Collections.emptyList();
        dashboardRepository.topProdutos = Collections.emptyList();

        DashboardResumoOutput output = useCase.executar(tenantIdRaw);

        assertNotNull(output);
        assertEquals(BigDecimal.ZERO, output.hoje().faturamentoTotal());
        assertEquals(0, output.hoje().totalVendas());
        assertEquals(BigDecimal.ZERO, output.totalFiadoNaRua());
        assertTrue(output.distribuicaoPagamentosHoje().isEmpty());
        assertTrue(output.topProdutosMes().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tenantId não for informado")
    void deveFalharQuandoTenantIdNaoInformado() {
        assertThrows(IllegalArgumentException.class, () -> useCase.executar(null));
    }

    // Fakes
    static class FakeDashboardRepository implements DashboardRepository {
        MetricasFaturamentoDTO metricasHoje;
        MetricasFaturamentoDTO metricasMes;
        List<TotalPorFormaPagamentoDTO> distribuicaoHoje = Collections.emptyList();
        List<TopProdutoVendidoDTO> topProdutos = Collections.emptyList();
        private int metricasCallCount = 0;

        @Override
        public MetricasFaturamentoDTO calcularMetricasFaturamento(TenantId tenantId, Instant de, Instant ate) {
            if (metricasCallCount++ % 2 == 0) {
                return metricasHoje != null ? metricasHoje : new MetricasFaturamentoDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO);
            }
            return metricasMes != null ? metricasMes : new MetricasFaturamentoDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO);
        }

        @Override
        public List<TotalPorFormaPagamentoDTO> obterDistribuicaoPagamentos(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal) {
            return distribuicaoHoje;
        }

        @Override
        public List<TopProdutoVendidoDTO> obterTopProdutos(TenantId tenantId, Instant de, Instant ate, int limite) {
            return topProdutos;
        }
    }

    static class FakeClienteRepository implements ClienteRepository {
        private final Map<UUID, Cliente> store = new HashMap<>();

        @Override
        public Optional<Cliente> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Cliente> buscarPorNomeOuTelefone(TenantId tenantId, String nome, String telefone) {
            return store.values().stream().filter(c -> c.getTenantId().equals(tenantId)).findFirst();
        }

        @Override
        public List<Cliente> listarComSaldoDevedor(TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.getSaldoDevedor().isMaiorQue(Dinheiro.zero()))
                .toList();
        }

        @Override
        public List<Cliente> buscarPorNome(String nome, TenantId tenantId) {
            return Collections.emptyList();
        }

        @Override
        public List<Cliente> listarTodos(TenantId tenantId, String busca, String status, Boolean apenasDevedores) {
            return Collections.emptyList();
        }

        @Override
        public void excluir(UUID id, TenantId tenantId) {
            store.remove(id);
        }

        @Override
        public void salvar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
        }

        @Override
        public void atualizar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
        }
    }
}
