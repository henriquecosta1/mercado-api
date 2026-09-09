package com.mercado.application;

import com.mercado.application.dto.ItemVendaInput;
import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.application.usecase.RegistrarVendaUseCase;
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

@DisplayName("Testes de Aplicação: Venda com Itens de Carrinho e Baixa de Estoque")
class VendaComItensUseCaseTest {

    private FakeCaixaRepository caixaRepository;
    private FakeClienteRepository clienteRepository;
    private FakeVendaRepository vendaRepository;
    private FakeProdutoRepository produtoRepository;
    private RegistrarVendaUseCase useCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        caixaRepository = new FakeCaixaRepository();
        clienteRepository = new FakeClienteRepository();
        vendaRepository = new FakeVendaRepository();
        produtoRepository = new FakeProdutoRepository();
        useCase = new RegistrarVendaUseCase(caixaRepository, clienteRepository, vendaRepository, produtoRepository);

        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixa);
    }

    @Test
    @DisplayName("Deve registrar venda com itens calculando subtotal e baixando estoque do produto")
    void deveRegistrarVendaComItensEBaixarEstoque() {
        Produto produto = Produto.criar(tenantId, "Café 500g", Dinheiro.de("18.00"), "PCT", new BigDecimal("20.000"));
        produtoRepository.salvar(produto);

        List<ItemVendaInput> itens = List.of(
            new ItemVendaInput(produto.getId(), "Café 500g", new BigDecimal("2.000"), new BigDecimal("18.00"))
        );

        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("36.00"),
            new BigDecimal("50.00"),
            "DINHEIRO",
            null,
            null,
            "Compra café",
            itens
        );

        RegistrarVendaOutput output = useCase.executar(input);

        assertNotNull(output.vendaId());
        assertEquals(new BigDecimal("36.00"), output.valorTotal());
        assertEquals(new BigDecimal("14.00"), output.troco());

        // Verifica que o estoque baixou de 20 para 18
        Produto produtoAtualizado = produtoRepository.buscarPorId(produto.getId(), tenantId).orElseThrow();
        assertEquals(new BigDecimal("18.000"), produtoAtualizado.getEstoqueAtual());

        // Verifica que os itens foram gravados na venda
        Venda vendaSalva = vendaRepository.buscarPorId(tenantId, output.vendaId()).orElseThrow();
        assertEquals(1, vendaSalva.getItens().size());
        assertEquals(new BigDecimal("36.00"), vendaSalva.getItens().get(0).getSubtotal().valor());
    }

    @Test
    @DisplayName("Deve falhar venda quando valor total informado for diferente da soma dos itens")
    void deveFalharTotalDivergenteDosItens() {
        List<ItemVendaInput> itens = List.of(
            new ItemVendaInput(null, "Pão Francês kg", new BigDecimal("1.000"), new BigDecimal("15.00"))
        );

        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("20.00"), // Divergente de 15.00
            new BigDecimal("20.00"),
            "DINHEIRO",
            null,
            null,
            "Compra",
            itens
        );

        assertThrows(RegraDeNegocioException.class, () -> useCase.executar(input));
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
        public List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
            return store.values().stream().filter(v -> v.getTenantId().equals(tenantId) && v.getCaixaId().equals(caixaId)).toList();
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
