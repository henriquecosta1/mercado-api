package com.mercado.application;

import com.mercado.application.dto.ProdutoCarrinhoDTO;
import com.mercado.application.usecase.BuscarProdutoPorCodigoUseCase;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.PageResult;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: BuscarProdutoPorCodigoUseCase (PDV - Leitor de Código de Barras e Balança)")
class BuscarProdutoPorCodigoUseCaseTest {

    private FakeProdutoRepository produtoRepository;
    private BuscarProdutoPorCodigoUseCase useCase;

    private final UUID tenant1 = UUID.randomUUID();
    private final UUID tenant2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        produtoRepository = new FakeProdutoRepository();
        useCase = new BuscarProdutoPorCodigoUseCase(produtoRepository);

        // Produto 1: Bebida com EAN-13 padrão (unidade UN, não fracionado)
        Produto refrigerante = Produto.criar(
            TenantId.de(tenant1),
            "Coca-Cola 2L",
            "Bebidas",
            Dinheiro.de("9.50"),
            Dinheiro.de("6.00"),
            Dinheiro.de("8.99"),
            "UN",
            new BigDecimal("100.000"),
            new BigDecimal("10.000"),
            "7894900011517",
            "1001",
            false
        );
        produtoRepository.salvar(refrigerante);

        // Produto 2: Queijo Mussarela pesado (unidade KG, fracionado, balança código 15 / 00015)
        Produto mussarela = Produto.criar(
            TenantId.de(tenant1),
            "Queijo Mussarela Peça",
            "Frios",
            Dinheiro.de("52.00"),
            Dinheiro.de("35.00"),
            null,
            "KG",
            new BigDecimal("25.400"),
            new BigDecimal("3.000"),
            null,
            "15",
            true
        );
        produtoRepository.salvar(mussarela);

        // Produto 3: Fruta com código interno 25 e código com zeros "00025"
        Produto maca = Produto.criar(
            TenantId.de(tenant1),
            "Maçã Fuji",
            "Hortifrúti",
            Dinheiro.de("11.90"),
            Dinheiro.de("7.00"),
            null,
            "KG",
            new BigDecimal("40.000"),
            new BigDecimal("5.000"),
            "7890000000250",
            "00025",
            true
        );
        produtoRepository.salvar(maca);

        // Produto 4: Pertencente ao Tenant 2 (para teste de isolamento multi-tenant)
        Produto cafeTenant2 = Produto.criar(
            TenantId.de(tenant2),
            "Café Torrado 500g",
            "Mercearia",
            Dinheiro.de("18.00"),
            Dinheiro.de("12.00"),
            null,
            "PCT",
            new BigDecimal("30.000"),
            new BigDecimal("5.000"),
            "7894900011517", // mesmo código de barras, mas tenant diferente
            "1001",
            false
        );
        produtoRepository.salvar(cafeTenant2);

        // Produto 5: Produto Inativo
        Produto inativo = Produto.criar(
            TenantId.de(tenant1),
            "Produto Descontinuado",
            "Geral",
            Dinheiro.de("10.00"),
            Dinheiro.de("5.00"),
            null,
            "UN",
            new BigDecimal("5.000"),
            new BigDecimal("1.000"),
            "7899999999999",
            "9999",
            false
        );
        inativo.inativar();
        produtoRepository.salvar(inativo);
    }

    @Test
    @DisplayName("Deve buscar produto por código de barras comercial EAN-13 com sucesso")
    void deveBuscarPorCodigoBarrasComercial() {
        ProdutoCarrinhoDTO resultado = useCase.executar(tenant1, "7894900011517");

        assertNotNull(resultado);
        assertEquals("Coca-Cola 2L", resultado.nome());
        assertEquals("Coca-Cola 2L", resultado.descricao());
        assertEquals(new BigDecimal("9.50"), resultado.precoVenda());
        assertEquals(new BigDecimal("8.99"), resultado.precoPromocional());
        assertEquals("UN", resultado.unidadeMedida());
        assertEquals("UN", resultado.unidade());
        assertEquals(new BigDecimal("100.000"), resultado.estoqueAtual());
        assertEquals("7894900011517", resultado.codigoBarras());
        assertFalse(resultado.permiteFracionado());
    }

    @Test
    @DisplayName("Deve buscar produto por código interno digitado pelo operador")
    void deveBuscarPorCodigoInternoDigitado() {
        ProdutoCarrinhoDTO resultado = useCase.executar(tenant1, "15");

        assertNotNull(resultado);
        assertEquals("Queijo Mussarela Peça", resultado.nome());
        assertEquals(new BigDecimal("52.00"), resultado.precoVenda());
        assertNull(resultado.precoPromocional());
        assertEquals("KG", resultado.unidadeMedida());
        assertTrue(resultado.permiteFracionado());
    }

    @Test
    @DisplayName("Deve identificar produto pesado ao bipar etiqueta de balança comercial iniciada em '2'")
    void deveIdentificarProdutoPesadoEtiquetaBalanca() {
        // Exemplo: Etiqueta de balança Toledo: 2 (prefixo balança) + 00015 (código item) + 00450 (preço/peso) + 8 (DV) = 13 dígitos
        String etiquetaBalanca = "2000150004508";

        ProdutoCarrinhoDTO resultado = useCase.executar(tenant1, etiquetaBalanca);

        assertNotNull(resultado);
        assertEquals("Queijo Mussarela Peça", resultado.nome());
        assertEquals(new BigDecimal("52.00"), resultado.precoVenda());
        assertEquals("KG", resultado.unidadeMedida());
        assertTrue(resultado.permiteFracionado());
    }

    @Test
    @DisplayName("Deve identificar produto de balança com código de 5 dígitos formatado com zeros")
    void deveIdentificarProdutoBalancaComZeros() {
        // Etiqueta balança com código "00025": 2 00025 00350 2
        String etiquetaBalanca = "2000250035021";

        ProdutoCarrinhoDTO resultado = useCase.executar(tenant1, etiquetaBalanca);

        assertNotNull(resultado);
        assertEquals("Maçã Fuji", resultado.nome());
        assertEquals(new BigDecimal("11.90"), resultado.precoVenda());
        assertEquals("KG", resultado.unidadeMedida());
        assertTrue(resultado.permiteFracionado());
    }

    @Test
    @DisplayName("Deve respeitar estritamente o isolamento multi-tenant")
    void deveRespeitarMultiTenancy() {
        // O tenant 2 possui um produto com o mesmo código "7894900011517", mas é o Café
        ProdutoCarrinhoDTO resultadoTenant2 = useCase.executar(tenant2, "7894900011517");
        assertEquals("Café Torrado 500g", resultadoTenant2.nome());

        // O tenant 1 obtém a Coca-Cola
        ProdutoCarrinhoDTO resultadoTenant1 = useCase.executar(tenant1, "7894900011517");
        assertEquals("Coca-Cola 2L", resultadoTenant1.nome());

        // O tenant 2 não pode encontrar produtos exclusivos do tenant 1 (ex: código interno 15)
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant2, "15")
        );
    }

    @Test
    @DisplayName("Não deve retornar produto inativo (deve lançar RecursoNaoEncontradoException)")
    void naoDeveRetornarProdutoInativo() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant1, "7899999999999")
        );
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant1, "9999")
        );
    }

    @Test
    @DisplayName("Deve falhar com 404 quando produto não for encontrado")
    void deveFalharQuandoProdutoNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant1, "9998887776665")
        );
    }

    @Test
    @DisplayName("Deve falhar quando tenantId for nulo")
    void deveFalharTenantIdNulo() {
        assertThrows(RegraDeNegocioException.class, () ->
            useCase.executar(null, "7894900011517")
        );
    }

    @Test
    @DisplayName("Deve falhar quando código for nulo ou em branco")
    void deveFalharCodigoNuloOuVazio() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant1, null)
        );
        assertThrows(RecursoNaoEncontradoException.class, () ->
            useCase.executar(tenant1, "   ")
        );
    }

    // =========================================================================
    // Fake Repository com simulação fiel da query HQL indexada
    // =========================================================================
    static class FakeProdutoRepository implements ProdutoRepository {
        private final Map<UUID, Produto> banco = new HashMap<>();

        @Override
        public void salvar(Produto produto) {
            banco.put(produto.getId(), produto);
        }

        @Override
        public Optional<Produto> buscarPorId(UUID id, TenantId tenantId) {
            Produto p = banco.get(id);
            if (p != null && p.getTenantId().equals(tenantId)) {
                return Optional.of(p);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Produto> findByCodigoOuCodigoBarras(UUID tenantId, String codigo) {
            if (tenantId == null || codigo == null || codigo.isBlank()) {
                return Optional.empty();
            }
            String c = codigo.trim();
            return banco.values().stream()
                .filter(p -> p.getTenantId().valor().equals(tenantId))
                .filter(Produto::isAtivo)
                .filter(p -> c.equalsIgnoreCase(p.getCodigoBarras()) || c.equalsIgnoreCase(p.getCodigoInterno()))
                .findFirst();
        }

        @Override
        public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
            return banco.values().stream()
                .filter(p -> p.getTenantId().equals(tenantId) && p.isAtivo())
                .filter(p -> p.getNome().toLowerCase().contains(termo.toLowerCase()))
                .toList();
        }

        @Override
        public List<Produto> listarAtivos(TenantId tenantId) {
            return banco.values().stream()
                .filter(p -> p.getTenantId().equals(tenantId) && p.isAtivo())
                .toList();
        }
    }
}
