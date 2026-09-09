package com.mercado.application;

import com.mercado.application.dto.CancelarVendaInput;
import com.mercado.application.dto.CancelarVendaOutput;
import com.mercado.application.dto.VendaResumoDTO;
import com.mercado.application.usecase.CancelarVendaUseCase;
import com.mercado.application.usecase.ListarVendasCaixaAtualUseCase;
import com.mercado.domain.entity.*;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: CancelarVendaUseCase e Histórico do Caixa")
class CancelarVendaUseCaseTest {

    private FakeCaixaRepository caixaRepository;
    private FakeClienteRepository clienteRepository;
    private FakeVendaRepository vendaRepository;
    private FakeProdutoRepository produtoRepository;
    private CancelarVendaUseCase cancelarVendaUseCase;
    private ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    private Caixa caixaAberto;

    @BeforeEach
    void setUp() {
        caixaRepository = new FakeCaixaRepository();
        clienteRepository = new FakeClienteRepository();
        vendaRepository = new FakeVendaRepository();
        produtoRepository = new FakeProdutoRepository();

        cancelarVendaUseCase = new CancelarVendaUseCase(
            vendaRepository,
            caixaRepository,
            clienteRepository,
            produtoRepository
        );

        listarVendasCaixaAtualUseCase = new ListarVendasCaixaAtualUseCase(
            caixaRepository,
            vendaRepository
        );

        caixaAberto = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixaAberto);
    }

    @Test
    @DisplayName("Deve cancelar venda em DINHEIRO e estornar saldo da gaveta do caixa")
    void deveCancelarVendaDinheiroEEstornarSaldoDoCaixa() {
        // Simula venda de 50.00 em dinheiro adicionada ao caixa (100 + 50 = 150)
        caixaAberto.adicionarDinheiro(Dinheiro.de("50.00"));
        caixaRepository.atualizar(caixaAberto);

        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("50.00"),
            FormaPagamento.DINHEIRO,
            Dinheiro.zero(),
            "Venda balcão"
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Cliente desistiu");
        CancelarVendaOutput output = cancelarVendaUseCase.executar(input);

        assertNotNull(output);
        assertEquals(venda.getId(), output.vendaId());
        assertEquals(StatusVenda.CANCELADA.name(), output.status());
        assertNotNull(output.canceladaEm());

        // Verifica estado da venda
        Venda vendaAtualizada = vendaRepository.buscarPorIdComItens(venda.getId(), tenantId).orElseThrow();
        assertTrue(vendaAtualizada.isCancelada());
        assertEquals("Cliente desistiu", vendaAtualizada.getMotivoCancelamento());

        // Verifica estorno no caixa (150 - 50 = 100)
        Caixa caixaAtualizado = caixaRepository.buscarPorId(tenantId, caixaAberto.getId()).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve cancelar venda em FIADO e estornar débito na conta do cliente")
    void deveCancelarVendaFiadoEEstornarDebitoCliente() {
        Cliente cliente = Cliente.criar(tenantId, "Maria Silva", "11988887777", Dinheiro.zero());
        cliente.registrarDebito(Dinheiro.de("80.00")); // débito de 80.00
        clienteRepository.salvar(cliente);

        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("80.00"),
            FormaPagamento.FIADO,
            Dinheiro.zero(),
            "Venda fiado",
            Collections.emptyList(),
            cliente.getId(),
            cliente.getNome()
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Lançado no cliente errado");
        CancelarVendaOutput output = cancelarVendaUseCase.executar(input);

        assertEquals(StatusVenda.CANCELADA.name(), output.status());

        // Verifica que o débito do cliente foi estornado (80 - 80 = 0.00)
        Cliente clienteAtualizado = clienteRepository.buscarPorId(tenantId, cliente.getId()).orElseThrow();
        assertEquals(new BigDecimal("0.00"), clienteAtualizado.getSaldoDevedor().valor());

        // Saldo do caixa não deve ter sido afetado
        Caixa caixaAtualizado = caixaRepository.buscarPorId(tenantId, caixaAberto.getId()).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve repor estoque dos produtos ao cancelar venda com itens")
    void deveReporEstoqueDosProdutosAoCancelarVendaComItens() {
        Produto p1 = Produto.criar(tenantId, "Arroz 5kg", Dinheiro.de("25.00"), "PCT", new BigDecimal("10.000"));
        Produto p2 = Produto.criar(tenantId, "Feijão 1kg", Dinheiro.de("8.00"), "PCT", new BigDecimal("15.000"));
        produtoRepository.salvar(p1);
        produtoRepository.salvar(p2);

        // Baixa manual simulando que a venda foi realizada
        p1.baixarEstoque(new BigDecimal("2.000")); // Resta 8
        p2.baixarEstoque(new BigDecimal("3.000")); // Resta 12
        produtoRepository.salvar(p1);
        produtoRepository.salvar(p2);

        List<ItemVenda> itens = List.of(
            ItemVenda.criar(p1.getId(), p1.getNome(), new BigDecimal("2.000"), p1.getPrecoVenda()),
            ItemVenda.criar(p2.getId(), p2.getNome(), new BigDecimal("3.000"), p2.getPrecoVenda())
        );

        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("74.00"),
            FormaPagamento.CARTAO,
            Dinheiro.zero(),
            "Venda com itens",
            itens,
            null,
            null
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Cancelamento total da compra");
        cancelarVendaUseCase.executar(input);

        // Verifica reposição de estoque
        Produto p1Atualizado = produtoRepository.buscarPorId(p1.getId(), tenantId).orElseThrow();
        Produto p2Atualizado = produtoRepository.buscarPorId(p2.getId(), tenantId).orElseThrow();

        assertEquals(new BigDecimal("10.000"), p1Atualizado.getEstoqueAtual());
        assertEquals(new BigDecimal("15.000"), p2Atualizado.getEstoqueAtual());
    }

    @Test
    @DisplayName("Deve falhar ao tentar cancelar venda quando o caixa já se encontra encerrado")
    void deveFalharAoCancelarVendaComCaixaFechado() {
        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("20.00"),
            FormaPagamento.DINHEIRO,
            Dinheiro.zero(),
            "Venda"
        );
        vendaRepository.salvar(venda);

        // Fecha o caixa
        caixaAberto.fechar();
        caixaRepository.atualizar(caixaAberto);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Tentativa estorno");

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
            () -> cancelarVendaUseCase.executar(input));
        assertTrue(ex.getMessage().contains("caixa já encerrado"));
    }

    @Test
    @DisplayName("Deve falhar ao tentar cancelar uma venda que já está cancelada")
    void deveFalharAoCancelarVendaJaCancelada() {
        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("20.00"),
            FormaPagamento.DINHEIRO,
            Dinheiro.zero(),
            "Venda"
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input1 = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Primeiro cancelamento");
        cancelarVendaUseCase.executar(input1);

        CancelarVendaInput input2 = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Segundo cancelamento");
        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
            () -> cancelarVendaUseCase.executar(input2));
        assertTrue(ex.getMessage().contains("já se encontra cancelada"));
    }

    @Test
    @DisplayName("Deve listar vendas do caixa atual ordenadas")
    void deveListarVendasCaixaAtual() {
        Venda v1 = Venda.criar(tenantId, caixaAberto.getId(), Dinheiro.de("10.00"), FormaPagamento.DINHEIRO, Dinheiro.zero(), "Venda 1");
        Venda v2 = Venda.criar(tenantId, caixaAberto.getId(), Dinheiro.de("20.00"), FormaPagamento.PIX, Dinheiro.zero(), "Venda 2");
        vendaRepository.salvar(v1);
        vendaRepository.salvar(v2);

        List<VendaResumoDTO> lista = listarVendasCaixaAtualUseCase.executar(tenantIdRaw);

        assertEquals(2, lista.size());
    }

    // Fakes
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
        public Optional<Venda> buscarPorIdComItens(UUID vendaId, TenantId tenantId) {
            return Optional.ofNullable(store.get(vendaId));
        }

        @Override
        public List<Venda> listarPorCaixa(UUID caixaId, TenantId tenantId) {
            return store.values().stream()
                .filter(v -> v.getTenantId().equals(tenantId) && v.getCaixaId().equals(caixaId))
                .toList();
        }
    }

    static class FakeProdutoRepository implements ProdutoRepository {
        private final Map<UUID, Produto> store = new HashMap<>();

        @Override
        public void salvar(Produto produto) {
            store.put(produto.getId(), produto);
        }

        @Override
        public Optional<Produto> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.ofNullable(store.get(id)).filter(p -> p.getTenantId().equals(tenantId));
        }

        @Override
        public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
            return store.values().stream().filter(p -> p.getTenantId().equals(tenantId)).toList();
        }

        @Override
        public List<Produto> listarAtivos(TenantId tenantId) {
            return store.values().stream().filter(p -> p.getTenantId().equals(tenantId) && p.isAtivo()).toList();
        }
    }
}
