package com.mercado.application;

import com.mercado.application.dto.AjustarEstoqueInput;
import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.application.dto.SalvarProdutoInput;
import com.mercado.application.usecase.AjustarEstoqueUseCase;
import com.mercado.application.usecase.AlternarStatusProdutoUseCase;
import com.mercado.application.usecase.ListarProdutosGerencialUseCase;
import com.mercado.application.usecase.SalvarProdutoUseCase;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: Módulo Gerencial de Produtos e Estoque")
class ProdutoGerencialUseCaseTest {

    private FakeProdutoRepository produtoRepository;
    private SalvarProdutoUseCase salvarProdutoUseCase;
    private ListarProdutosGerencialUseCase listarProdutosGerencialUseCase;
    private AjustarEstoqueUseCase ajustarEstoqueUseCase;
    private AlternarStatusProdutoUseCase alternarStatusProdutoUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        produtoRepository = new FakeProdutoRepository();
        salvarProdutoUseCase = new SalvarProdutoUseCase(produtoRepository);
        listarProdutosGerencialUseCase = new ListarProdutosGerencialUseCase(produtoRepository);
        ajustarEstoqueUseCase = new AjustarEstoqueUseCase(produtoRepository);
        alternarStatusProdutoUseCase = new AlternarStatusProdutoUseCase(produtoRepository);
    }

    @Test
    @DisplayName("Deve cadastrar novo produto gerencial com categoria e estoque mínimo")
    void deveCadastrarNovoProdutoGerencial() {
        SalvarProdutoInput input = new SalvarProdutoInput(
            tenantIdRaw,
            null,
            "Arroz Parboilizado 5kg",
            "Mercearia",
            new BigDecimal("29.90"),
            new BigDecimal("21.50"),
            "PCT",
            new BigDecimal("40.000"),
            new BigDecimal("10.000")
        );

        ProdutoGerencialDTO dto = salvarProdutoUseCase.executar(input);

        assertNotNull(dto.id());
        assertEquals("Arroz Parboilizado 5kg", dto.nome());
        assertEquals("Mercearia", dto.categoria());
        assertEquals(new BigDecimal("29.90"), dto.precoVenda());
        assertEquals(new BigDecimal("21.50"), dto.precoCusto());
        assertEquals(new BigDecimal("40.000"), dto.estoqueAtual());
        assertEquals(new BigDecimal("10.000"), dto.estoqueMinimo());
        assertTrue(dto.ativo());
        assertFalse(dto.alertaEstoqueBaixo());
    }

    @Test
    @DisplayName("Deve atualizar produto existente")
    void deveAtualizarProdutoExistente() {
        SalvarProdutoInput cadastroInput = new SalvarProdutoInput(
            tenantIdRaw,
            null,
            "Feijão Preto 1kg",
            "Mercearia",
            new BigDecimal("7.00"),
            new BigDecimal("5.00"),
            "PCT",
            new BigDecimal("20.000"),
            new BigDecimal("5.000")
        );
        ProdutoGerencialDTO criado = salvarProdutoUseCase.executar(cadastroInput);

        SalvarProdutoInput updateInput = new SalvarProdutoInput(
            tenantIdRaw,
            criado.id(),
            "Feijão Preto Premium 1kg",
            "Grãos",
            new BigDecimal("8.50"),
            new BigDecimal("6.00"),
            "PCT",
            null,
            new BigDecimal("8.000")
        );

        ProdutoGerencialDTO atualizado = salvarProdutoUseCase.executar(updateInput);

        assertEquals(criado.id(), atualizado.id());
        assertEquals("Feijão Preto Premium 1kg", atualizado.nome());
        assertEquals("Grãos", atualizado.categoria());
        assertEquals(new BigDecimal("8.50"), atualizado.precoVenda());
        assertEquals(new BigDecimal("8.000"), atualizado.estoqueMinimo());
    }

    @Test
    @DisplayName("Deve ajustar estoque físico manualmente")
    void deveAjustarEstoqueFisico() {
        SalvarProdutoInput input = new SalvarProdutoInput(
            tenantIdRaw,
            null,
            "Leite Condensado 395g",
            "Doces",
            new BigDecimal("6.50"),
            new BigDecimal("4.50"),
            "UN",
            new BigDecimal("50.000"),
            new BigDecimal("10.000")
        );
        ProdutoGerencialDTO produto = salvarProdutoUseCase.executar(input);

        AjustarEstoqueInput ajuste = new AjustarEstoqueInput(
            tenantIdRaw,
            produto.id(),
            new BigDecimal("42.000"),
            "Contagem física da prateleira"
        );

        ProdutoGerencialDTO ajustado = ajustarEstoqueUseCase.executar(ajuste);

        assertEquals(new BigDecimal("42.000"), ajustado.estoqueAtual());
    }

    @Test
    @DisplayName("Deve alternar status do produto (ativo/inativo)")
    void deveAlternarStatusProduto() {
        SalvarProdutoInput input = new SalvarProdutoInput(
            tenantIdRaw,
            null,
            "Desinfetante Lavanda 1L",
            "Limpeza",
            new BigDecimal("5.00"),
            new BigDecimal("3.00"),
            "UN",
            new BigDecimal("15.000"),
            new BigDecimal("5.000")
        );
        ProdutoGerencialDTO produto = salvarProdutoUseCase.executar(input);
        assertTrue(produto.ativo());

        // Inativa
        ProdutoGerencialDTO inativado = alternarStatusProdutoUseCase.executar(tenantIdRaw, produto.id());
        assertFalse(inativado.ativo());

        // Reativa
        ProdutoGerencialDTO reativado = alternarStatusProdutoUseCase.executar(tenantIdRaw, produto.id());
        assertTrue(reativado.ativo());
    }

    @Test
    @DisplayName("Deve filtrar produtos por busca, categoria e alerta de estoque baixo")
    void deveFiltrarProdutosGerencial() {
        // Produto 1: Bebida, estoque baixo (3 <= 5)
        salvarProdutoUseCase.executar(new SalvarProdutoInput(tenantIdRaw, null, "Cerveja Lata", "Bebidas", new BigDecimal("4.00"), null, "UN", new BigDecimal("3.000"), new BigDecimal("5.000")));
        // Produto 2: Bebida, estoque normal (20 > 5)
        salvarProdutoUseCase.executar(new SalvarProdutoInput(tenantIdRaw, null, "Vinho Tinto", "Bebidas", new BigDecimal("35.00"), null, "UN", new BigDecimal("20.000"), new BigDecimal("5.000")));
        // Produto 3: Limpeza, estoque normal (15 > 5)
        salvarProdutoUseCase.executar(new SalvarProdutoInput(tenantIdRaw, null, "Sabão Barra", "Limpeza", new BigDecimal("2.50"), null, "UN", new BigDecimal("15.000"), new BigDecimal("5.000")));

        // Filtro por busca
        List<ProdutoGerencialDTO> buscaNome = listarProdutosGerencialUseCase.executar(tenantIdRaw, "cerveja", null, false);
        assertEquals(1, buscaNome.size());

        // Filtro por categoria
        List<ProdutoGerencialDTO> buscaCat = listarProdutosGerencialUseCase.executar(tenantIdRaw, null, "Bebidas", false);
        assertEquals(2, buscaCat.size());

        // Filtro por estoque baixo
        List<ProdutoGerencialDTO> estoqueBaixo = listarProdutosGerencialUseCase.executar(tenantIdRaw, null, null, true);
        assertEquals(1, estoqueBaixo.size());
        assertEquals("Cerveja Lata", estoqueBaixo.get(0).nome());
        assertTrue(estoqueBaixo.get(0).alertaEstoqueBaixo());
    }

    @Test
    @DisplayName("Deve paginar produtos corretamente calculando totalElements, totalPages, first e last")
    void devePaginarProdutosCorretamente() {
        for (int i = 1; i <= 25; i++) {
            salvarProdutoUseCase.executar(new SalvarProdutoInput(
                tenantIdRaw, null, String.format("Produto %02d", i), "Geral",
                new BigDecimal("10.00"), null, "UN", new BigDecimal("10.000"), new BigDecimal("2.000")
            ));
        }

        // Página 0, tamanho 10 -> itens 1 a 10
        var pag0 = listarProdutosGerencialUseCase.executarPaginado(tenantIdRaw, null, null, false, 0, 10);
        assertEquals(10, pag0.content().size());
        assertEquals(25L, pag0.totalElements());
        assertEquals(3, pag0.totalPages());
        assertEquals(0, pag0.page());
        assertTrue(pag0.first());
        assertFalse(pag0.last());
        assertEquals("Produto 01", pag0.content().get(0).nome());

        // Página 1, tamanho 10 -> itens 11 a 20
        var pag1 = listarProdutosGerencialUseCase.executarPaginado(tenantIdRaw, null, null, false, 1, 10);
        assertEquals(10, pag1.content().size());
        assertEquals(1, pag1.page());
        assertFalse(pag1.first());
        assertFalse(pag1.last());
        assertEquals("Produto 11", pag1.content().get(0).nome());

        // Página 2, tamanho 10 -> itens 21 a 25
        var pag2 = listarProdutosGerencialUseCase.executarPaginado(tenantIdRaw, null, null, false, 2, 10);
        assertEquals(5, pag2.content().size());
        assertEquals(2, pag2.page());
        assertFalse(pag2.first());
        assertTrue(pag2.last());
        assertEquals("Produto 21", pag2.content().get(0).nome());
    }

    static class FakeProdutoRepository implements ProdutoRepository {
        private final Map<UUID, Produto> store = new HashMap<>();

        @Override
        public void salvar(Produto produto) {
            store.put(produto.getId(), produto);
        }

        @Override
        public Optional<Produto> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.ofNullable(store.get(id))
                .filter(p -> p.getTenantId().equals(tenantId));
        }

        @Override
        public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
            return store.values().stream()
                .filter(p -> p.getTenantId().equals(tenantId) && p.isAtivo())
                .filter(p -> p.getNome().toLowerCase().contains(termo.toLowerCase()))
                .toList();
        }

        @Override
        public List<Produto> listarAtivos(TenantId tenantId) {
            return store.values().stream()
                .filter(p -> p.getTenantId().equals(tenantId) && p.isAtivo())
                .toList();
        }

        @Override
        public List<Produto> listarGerencial(TenantId tenantId, String busca, String categoria, Boolean apenasEstoqueBaixo) {
            return store.values().stream()
                .filter(p -> p.getTenantId().equals(tenantId))
                .filter(p -> busca == null || busca.isBlank() || p.getNome().toLowerCase().contains(busca.toLowerCase()))
                .filter(p -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase("Todas") || p.getCategoria().equalsIgnoreCase(categoria))
                .filter(p -> apenasEstoqueBaixo == null || !apenasEstoqueBaixo || p.isEstoqueAbaixoDoMinimo())
                .sorted(Comparator.comparing(Produto::getNome))
                .toList();
        }

        @Override
        public void excluirOuInativar(UUID id, TenantId tenantId) {
            buscarPorId(id, tenantId).ifPresent(Produto::inativar);
        }
    }
}
