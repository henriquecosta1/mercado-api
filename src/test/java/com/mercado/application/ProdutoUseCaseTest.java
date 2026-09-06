package com.mercado.application;

import com.mercado.application.dto.CriarProdutoInput;
import com.mercado.application.dto.ProdutoDTO;
import com.mercado.application.usecase.BuscarProdutosPorNomeUseCase;
import com.mercado.application.usecase.CadastrarProdutoUseCase;
import com.mercado.application.usecase.ListarProdutosUseCase;
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

@DisplayName("Testes de Aplicação: Catálogo de Produtos")
class ProdutoUseCaseTest {

    private FakeProdutoRepository produtoRepository;
    private CadastrarProdutoUseCase cadastrarProdutoUseCase;
    private BuscarProdutosPorNomeUseCase buscarProdutosPorNomeUseCase;
    private ListarProdutosUseCase listarProdutosUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        produtoRepository = new FakeProdutoRepository();
        cadastrarProdutoUseCase = new CadastrarProdutoUseCase(produtoRepository);
        buscarProdutosPorNomeUseCase = new BuscarProdutosPorNomeUseCase(produtoRepository);
        listarProdutosUseCase = new ListarProdutosUseCase(produtoRepository);
    }

    @Test
    @DisplayName("Deve cadastrar produto com sucesso")
    void deveCadastrarProduto() {
        CriarProdutoInput input = new CriarProdutoInput(
            tenantIdRaw,
            "Refrigerante Cola 2L",
            new BigDecimal("9.50"),
            "UN",
            new BigDecimal("24.000")
        );

        ProdutoDTO dto = cadastrarProdutoUseCase.executar(input);

        assertNotNull(dto.id());
        assertEquals("Refrigerante Cola 2L", dto.nome());
        assertEquals(new BigDecimal("9.50"), dto.precoVenda());
        assertEquals("UN", dto.unidade());
        assertEquals(new BigDecimal("24.000"), dto.estoqueAtual());
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar produto com preço negativo ou zero")
    void deveFalharPrecoInvalido() {
        CriarProdutoInput input = new CriarProdutoInput(
            tenantIdRaw,
            "Produto Teste",
            BigDecimal.ZERO,
            "UN",
            new BigDecimal("10.000")
        );

        assertThrows(RegraDeNegocioException.class, () -> cadastrarProdutoUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve buscar produtos por nome com busca textual case-insensitive")
    void deveBuscarPorNome() {
        cadastrarProdutoUseCase.executar(new CriarProdutoInput(tenantIdRaw, "Leite Integral 1L", new BigDecimal("5.00"), "UN", new BigDecimal("10.000")));
        cadastrarProdutoUseCase.executar(new CriarProdutoInput(tenantIdRaw, "Leite Desnatado 1L", new BigDecimal("5.20"), "UN", new BigDecimal("10.000")));
        cadastrarProdutoUseCase.executar(new CriarProdutoInput(tenantIdRaw, "Biscoito Recheado", new BigDecimal("3.50"), "UN", new BigDecimal("15.000")));

        List<ProdutoDTO> resultados = buscarProdutosPorNomeUseCase.executar(tenantIdRaw, "leite");

        assertEquals(2, resultados.size());
    }

    @Test
    @DisplayName("Deve listar produtos ativos quando termo for vazio")
    void deveListarAtivos() {
        cadastrarProdutoUseCase.executar(new CriarProdutoInput(tenantIdRaw, "Detergente Neutro", new BigDecimal("2.50"), "UN", new BigDecimal("50.000")));
        cadastrarProdutoUseCase.executar(new CriarProdutoInput(tenantIdRaw, "Sabão em Pó", new BigDecimal("12.00"), "UN", new BigDecimal("20.000")));

        List<ProdutoDTO> lista = listarProdutosUseCase.executar(tenantIdRaw, null);

        assertEquals(2, lista.size());
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
    }
}
