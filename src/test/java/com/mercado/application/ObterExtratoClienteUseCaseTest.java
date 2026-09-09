package com.mercado.application;

import com.mercado.application.dto.AmortizarFiadoInput;
import com.mercado.application.dto.ExtratoClienteOutput;
import com.mercado.application.dto.TransacaoExtratoDTO;
import com.mercado.application.usecase.AmortizarFiadoUseCase;
import com.mercado.application.usecase.ObterExtratoClienteUseCase;
import com.mercado.domain.entity.*;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.AmortizacaoRepository;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: ObterExtratoClienteUseCase e Histórico de Amortizações")
class ObterExtratoClienteUseCaseTest {

    private FakeClienteRepository clienteRepository;
    private FakeVendaRepository vendaRepository;
    private FakeAmortizacaoRepository amortizacaoRepository;
    private FakeCaixaRepository caixaRepository;

    private ObterExtratoClienteUseCase extratoUseCase;
    private AmortizarFiadoUseCase amortizarFiadoUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        clienteRepository = new FakeClienteRepository();
        vendaRepository = new FakeVendaRepository();
        amortizacaoRepository = new FakeAmortizacaoRepository();
        caixaRepository = new FakeCaixaRepository();

        extratoUseCase = new ObterExtratoClienteUseCase(
            clienteRepository,
            vendaRepository,
            amortizacaoRepository
        );

        amortizarFiadoUseCase = new AmortizarFiadoUseCase(
            clienteRepository,
            caixaRepository,
            amortizacaoRepository
        );

        // Caixa aberto
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixa);
    }

    @Test
    @DisplayName("Deve consolidar extrato detalhado com compras e pagamentos ordenados cronologicamente")
    void deveObterExtratoClienteComComprasEAmortizacoes() {
        // Cliente com limite e dívida atual
        Cliente cliente = Cliente.criar(tenantId, "Manoel Gomes", "11988887777", Dinheiro.de("500.00"));
        cliente.registrarDebito(Dinheiro.de("120.00"));
        clienteRepository.salvar(cliente);

        UUID caixaId = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow().getId();

        // 1. Compra mais antiga (10:00) com 2 itens
        Instant t1 = Instant.parse("2026-09-06T10:00:00Z");
        List<ItemVenda> itensV1 = List.of(
            ItemVenda.criar(UUID.randomUUID(), "Café Torrado", new BigDecimal("2.000"), Dinheiro.de("20.00")),
            ItemVenda.criar(UUID.randomUUID(), "Biscoito", new BigDecimal("1.000"), Dinheiro.de("10.00"))
        );
        Venda v1 = new Venda(
            UUID.randomUUID(),
            tenantId,
            caixaId,
            Dinheiro.de("50.00"),
            FormaPagamento.FIADO,
            Dinheiro.zero(),
            "Compra semanal",
            itensV1,
            StatusVenda.CONCLUIDA,
            cliente.getId(),
            cliente.getNome(),
            null,
            null,
            t1
        );
        vendaRepository.salvar(v1);

        // 2. Pagamento intermediário (12:00)
        Instant t2 = Instant.parse("2026-09-06T12:00:00Z");
        Amortizacao a1 = new Amortizacao(
            UUID.randomUUID(),
            tenantId,
            cliente.getId(),
            caixaId,
            Dinheiro.de("30.00"),
            "DINHEIRO",
            t2
        );
        amortizacaoRepository.salvar(a1);

        // 3. Compra mais recente (14:00) com 1 item
        Instant t3 = Instant.parse("2026-09-06T14:00:00Z");
        List<ItemVenda> itensV2 = List.of(
            ItemVenda.criar(UUID.randomUUID(), "Arroz 5kg", new BigDecimal("1.000"), Dinheiro.de("30.00"))
        );
        Venda v2 = new Venda(
            UUID.randomUUID(),
            tenantId,
            caixaId,
            Dinheiro.de("30.00"),
            FormaPagamento.FIADO,
            Dinheiro.zero(),
            "Compra almoço",
            itensV2,
            StatusVenda.CONCLUIDA,
            cliente.getId(),
            cliente.getNome(),
            null,
            null,
            t3
        );
        vendaRepository.salvar(v2);

        // Execução
        ExtratoClienteOutput extrato = extratoUseCase.executar(tenantIdRaw, cliente.getId());

        assertNotNull(extrato);
        assertEquals(cliente.getId(), extrato.clienteId());
        assertEquals("Manoel Gomes", extrato.nomeCliente());
        assertEquals("11988887777", extrato.telefone());
        assertEquals(new BigDecimal("120.00"), extrato.saldoDevedorAtual());
        assertEquals(new BigDecimal("500.00"), extrato.limiteCredito());

        // Validação da linha do tempo (deve conter 3 transações ordenadas da mais recente para a mais antiga)
        List<TransacaoExtratoDTO> transacoes = extrato.transacoes();
        assertEquals(3, transacoes.size());

        // Transação 1 (mais recente: 14:00, COMPRA)
        assertEquals("COMPRA", transacoes.get(0).tipo());
        assertEquals(new BigDecimal("30.00"), transacoes.get(0).valor());
        assertEquals(1, transacoes.get(0).itens().size());
        assertEquals("Arroz 5kg", transacoes.get(0).itens().get(0).descricao());
        assertEquals(t3, transacoes.get(0).dataHora());

        // Transação 2 (intermediária: 12:00, PAGAMENTO)
        assertEquals("PAGAMENTO", transacoes.get(1).tipo());
        assertEquals(new BigDecimal("30.00"), transacoes.get(1).valor());
        assertEquals("DINHEIRO", transacoes.get(1).formaPagamento());
        assertTrue(transacoes.get(1).itens().isEmpty());
        assertEquals(t2, transacoes.get(1).dataHora());

        // Transação 3 (mais antiga: 10:00, COMPRA)
        assertEquals("COMPRA", transacoes.get(2).tipo());
        assertEquals(new BigDecimal("50.00"), transacoes.get(2).valor());
        assertEquals(2, transacoes.get(2).itens().size());
        assertEquals(t1, transacoes.get(2).dataHora());
    }

    @Test
    @DisplayName("Deve persistir amortização no repositório ao executar AmortizarFiadoUseCase")
    void devePersistirAmortizacaoAoAmortizar() {
        Cliente cliente = Cliente.criar(tenantId, "Sebastião", "11999990000", Dinheiro.de("200.00"));
        cliente.registrarDebito(Dinheiro.de("100.00"));
        clienteRepository.salvar(cliente);

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            cliente.getId(),
            new BigDecimal("40.00"),
            "PIX"
        );

        amortizarFiadoUseCase.executar(input);

        // Verifica amortização gravada
        List<Amortizacao> historico = amortizacaoRepository.listarPorCliente(cliente.getId(), tenantId);
        assertEquals(1, historico.size());
        assertEquals(new BigDecimal("40.00"), historico.get(0).getValor().valor());
        assertEquals("PIX", historico.get(0).getFormaPagamento());
    }

    @Test
    @DisplayName("Deve falhar ao tentar obter extrato de cliente não encontrado")
    void deveFalharClienteNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class,
            () -> extratoUseCase.executar(tenantIdRaw, UUID.randomUUID()));
    }

    // Fakes em memória
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
            return Collections.emptyList();
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

    static class FakeVendaRepository implements VendaRepository {
        private final Map<UUID, Venda> store = new HashMap<>();

        @Override
        public void salvar(Venda venda) {
            store.put(venda.getId(), venda);
        }

        @Override
        public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
            return Collections.emptyList();
        }

        @Override
        public List<Venda> listarFiadoPorCliente(UUID clienteId, TenantId tenantId) {
            return store.values().stream()
                .filter(v -> v.getTenantId().equals(tenantId) && clienteId.equals(v.getClienteId()))
                .filter(v -> v.getFormaPagamento() == FormaPagamento.FIADO && !v.isCancelada())
                .toList();
        }
    }

    static class FakeAmortizacaoRepository implements AmortizacaoRepository {
        private final List<Amortizacao> store = new ArrayList<>();

        @Override
        public void salvar(Amortizacao amortizacao) {
            store.add(amortizacao);
        }

        @Override
        public List<Amortizacao> listarPorCliente(UUID clienteId, TenantId tenantId) {
            return store.stream()
                .filter(a -> a.getTenantId().equals(tenantId) && a.getClienteId().equals(clienteId))
                .toList();
        }
    }

    static class FakeCaixaRepository implements CaixaRepository {
        private final Map<UUID, Caixa> store = new HashMap<>();

        @Override
        public Optional<Caixa> buscarCaixaAberto(TenantId tenantId) {
            return store.values().stream().filter(c -> c.getTenantId().equals(tenantId) && c.isAberto()).findFirst();
        }

        @Override
        public Optional<Caixa> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void salvar(Caixa caixa) {
            store.put(caixa.getId(), caixa);
        }

        @Override
        public void atualizar(Caixa caixa) {
            store.put(caixa.getId(), caixa);
        }
    }
}
