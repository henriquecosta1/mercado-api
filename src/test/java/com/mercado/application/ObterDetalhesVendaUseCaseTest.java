package com.mercado.application;

import com.mercado.application.dto.VendaDetalheResponseDTO;
import com.mercado.application.usecase.ObterDetalhesVendaUseCase;
import com.mercado.domain.entity.*;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
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

@DisplayName("Testes de Aplicação: ObterDetalhesVendaUseCase")
class ObterDetalhesVendaUseCaseTest {

    private FakeVendaRepository vendaRepository;
    private FakeClienteRepository clienteRepository;
    private FakeProdutoRepository produtoRepository;
    private ObterDetalhesVendaUseCase useCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        vendaRepository = new FakeVendaRepository();
        clienteRepository = new FakeClienteRepository();
        produtoRepository = new FakeProdutoRepository();
        useCase = new ObterDetalhesVendaUseCase(vendaRepository, clienteRepository, produtoRepository);
    }

    @Test
    @DisplayName("Deve retornar detalhes completos da venda com itens, produto e cliente vinculado")
    void deveRetornarDetalhesCompletosDaVendaComClienteEItens() {
        // Criar produto com unidade 'KG'
        Produto produto = Produto.criar(tenantId, "Carne Bovina", "Açougue", Dinheiro.de("45.00"), Dinheiro.de("30.00"), "KG", new BigDecimal("10.000"), new BigDecimal("2.000"));
        produtoRepository.salvar(produto);

        // Criar cliente
        Cliente cliente = Cliente.criar(tenantId, "Carlos Silva", "11988887777", Dinheiro.de("500.00"));
        clienteRepository.salvar(cliente);

        // Criar itens da venda
        ItemVenda item1 = ItemVenda.criar(produto.getId(), "Carne Bovina", new BigDecimal("1.500"), Dinheiro.de("45.00"));
        ItemVenda item2 = ItemVenda.criar(null, "Sacola Plastica", new BigDecimal("1.000"), Dinheiro.de("0.50"));

        UUID caixaId = UUID.randomUUID();
        Venda venda = Venda.criar(
            tenantId,
            caixaId,
            Dinheiro.de("68.00"),
            FormaPagamento.PIX,
            Dinheiro.zero(),
            "Venda balcão",
            List.of(item1, item2),
            cliente.getId(),
            cliente.getNome()
        );
        vendaRepository.salvar(venda);

        VendaDetalheResponseDTO detalhe = useCase.executar(tenantIdRaw, venda.getId());

        assertNotNull(detalhe);
        assertEquals(venda.getId(), detalhe.id());
        assertEquals(new BigDecimal("68.00"), detalhe.valorTotal());
        assertEquals("PIX", detalhe.formaPagamento());
        assertEquals("CONCLUIDA", detalhe.status());
        assertNotNull(detalhe.dataHora());

        // Cliente
        assertNotNull(detalhe.cliente());
        assertEquals(cliente.getId(), detalhe.cliente().id());
        assertEquals("Carlos Silva", detalhe.cliente().nome());
        assertEquals("11988887777", detalhe.cliente().telefone());

        // Itens
        assertEquals(2, detalhe.itens().size());
        var itemCarne = detalhe.itens().get(0);
        assertEquals(produto.getId(), itemCarne.produtoId());
        assertEquals("Carne Bovina", itemCarne.nomeProduto());
        assertEquals("KG", itemCarne.unidadeMedida());
        assertEquals(new BigDecimal("1.500"), itemCarne.quantidade());
        assertEquals(new BigDecimal("45.00"), itemCarne.precoUnitario());
        assertEquals(new BigDecimal("67.50"), itemCarne.subtotal());

        var itemSacola = detalhe.itens().get(1);
        assertNull(itemSacola.produtoId());
        assertEquals("Sacola Plastica", itemSacola.nomeProduto());
        assertEquals("UN", itemSacola.unidadeMedida());
        assertEquals(new BigDecimal("1.000"), itemSacola.quantidade());
        assertEquals(new BigDecimal("0.50"), itemSacola.precoUnitario());
        assertEquals(new BigDecimal("0.50"), itemSacola.subtotal());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando a venda não existir")
    void deveLancarExcecaoQuandoVendaNaoEncontrada() {
        UUID vendaInexistente = UUID.randomUUID();
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(tenantIdRaw, vendaInexistente));
    }

    @Test
    @DisplayName("Deve falhar se tenantId ou vendaId forem nulos")
    void deveValidarArgumentosObrigatorios() {
        assertThrows(IllegalArgumentException.class, () -> useCase.executar(null, UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> useCase.executar(tenantIdRaw, null));
    }

    // Fakes
    static class FakeVendaRepository implements VendaRepository {
        private final Map<UUID, Venda> store = new HashMap<>();

        @Override
        public void salvar(Venda venda) {
            store.put(venda.getId(), venda);
        }

        @Override
        public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
            return buscarPorIdComItens(id, tenantId);
        }

        @Override
        public Optional<Venda> buscarPorIdComItens(UUID vendaId, TenantId tenantId) {
            Venda v = store.get(vendaId);
            if (v != null && v.getTenantId().equals(tenantId)) {
                return Optional.of(v);
            }
            return Optional.empty();
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

    static class FakeProdutoRepository implements ProdutoRepository {
        private final Map<UUID, Produto> store = new HashMap<>();

        @Override
        public void salvar(Produto produto) {
            store.put(produto.getId(), produto);
        }

        @Override
        public Optional<Produto> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
            return Collections.emptyList();
        }

        @Override
        public List<Produto> listarAtivos(TenantId tenantId) {
            return Collections.emptyList();
        }

        @Override
        public List<Produto> listarGerencial(TenantId tenantId, String busca, String categoria, Boolean apenasEstoqueBaixo) {
            return Collections.emptyList();
        }

        @Override
        public void excluirOuInativar(UUID id, TenantId tenantId) {
            store.remove(id);
        }
    }
}
