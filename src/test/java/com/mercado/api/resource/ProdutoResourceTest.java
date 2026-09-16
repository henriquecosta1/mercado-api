package com.mercado.api.resource;

import com.mercado.api.dto.ErrorResponse;
import com.mercado.application.dto.ProdutoCarrinhoDTO;
import com.mercado.application.usecase.*;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Unidade: ProdutoResource (Endpoints de Checkout PDV)")
class ProdutoResourceTest {

    private ProdutoResource resource;
    private FakeProdutoRepository produtoRepository;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        produtoRepository = new FakeProdutoRepository();

        // Produto padrão EAN-13
        Produto p1 = Produto.criar(
            TenantId.de(tenantId),
            "Arroz Parboilizado 5kg",
            "Mercearia",
            Dinheiro.de("26.90"),
            Dinheiro.de("19.50"),
            Dinheiro.de("24.90"),
            "PCT",
            new BigDecimal("40.000"),
            new BigDecimal("5.000"),
            "7896001002003",
            "101",
            false
        );
        produtoRepository.salvar(p1);

        // Produto pesado balança
        Produto pKg = Produto.criar(
            TenantId.de(tenantId),
            "Alcatra Bovina",
            "Açougue",
            Dinheiro.de("48.90"),
            Dinheiro.de("32.00"),
            null,
            "KG",
            new BigDecimal("15.000"),
            new BigDecimal("2.000"),
            null,
            "20",
            true
        );
        produtoRepository.salvar(pKg);

        BuscarProdutoPorCodigoUseCase buscarUseCase = new BuscarProdutoPorCodigoUseCase(produtoRepository);

        JsonWebToken jwt = new FakeJwt(tenantId);
        TenantSecurityContext securityContext = new TenantSecurityContext(jwt);

        ListarProdutosGerencialUseCase listarGerencial = new ListarProdutosGerencialUseCase(produtoRepository);
        SalvarProdutoUseCase salvarUseCase = new SalvarProdutoUseCase(produtoRepository);
        AjustarEstoqueUseCase ajustarEstoque = new AjustarEstoqueUseCase(produtoRepository);
        AlternarStatusProdutoUseCase alternarStatus = new AlternarStatusProdutoUseCase(produtoRepository);

        resource = new ProdutoResource(
            listarGerencial,
            salvarUseCase,
            ajustarEstoque,
            alternarStatus,
            buscarUseCase,
            produtoRepository,
            securityContext
        );
    }

    @Test
    @DisplayName("GET /api/produtos/codigo/{codigo} deve retornar HTTP 200 com dados completos para o carrinho")
    void deveRetornar200AoBuscarPorCodigoPath() {
        Response response = resource.buscarPorCodigo("7896001002003");

        assertNotNull(response);
        assertEquals(200, response.getStatus());

        ProdutoCarrinhoDTO dto = (ProdutoCarrinhoDTO) response.getEntity();
        assertNotNull(dto);
        assertEquals("Arroz Parboilizado 5kg", dto.nome());
        assertEquals(new BigDecimal("26.90"), dto.precoVenda());
        assertEquals(new BigDecimal("24.90"), dto.precoPromocional());
        assertEquals("PCT", dto.unidadeMedida());
        assertEquals(new BigDecimal("40.000"), dto.estoqueAtual());
        assertEquals("7896001002003", dto.codigoBarras());
        assertFalse(dto.permiteFracionado());
    }

    @Test
    @DisplayName("GET /api/produtos/buscar-codigo?codigo={codigo} deve retornar HTTP 200 com produto de balança")
    void deveRetornar200AoBuscarPorCodigoQueryParam() {
        // Leitura de balança de 13 dígitos: 2 (prefixo) + 00020 (código item 20) + 00500 (peso/valor) + 1 (DV)
        Response response = resource.buscarPorCodigoParam("2000200005001");

        assertNotNull(response);
        assertEquals(200, response.getStatus());

        ProdutoCarrinhoDTO dto = (ProdutoCarrinhoDTO) response.getEntity();
        assertNotNull(dto);
        assertEquals("Alcatra Bovina", dto.nome());
        assertEquals(new BigDecimal("48.90"), dto.precoVenda());
        assertEquals("KG", dto.unidadeMedida());
        assertTrue(dto.permiteFracionado());
    }

    @Test
    @DisplayName("GET /api/produtos/codigo/{codigo} deve retornar HTTP 404 padronizado sem erro 500 no servidor")
    void deveRetornar404AoNaoEncontrarProduto() {
        Response response = resource.buscarPorCodigo("0000000000000");

        assertNotNull(response);
        assertEquals(404, response.getStatus());

        ErrorResponse erro = (ErrorResponse) response.getEntity();
        assertNotNull(erro);
        assertEquals(404, erro.status());
        assertEquals("Recurso Não Encontrado", erro.erro());
        assertTrue(erro.mensagem().contains("0000000000000"));
    }

    // =========================================================================
    // Fakes para teste desacoplado
    // =========================================================================
    static class FakeJwt implements JsonWebToken {
        private final UUID tenantId;

        public FakeJwt(UUID tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public String getName() {
            return "operador";
        }

        @Override
        public String getSubject() {
            return UUID.randomUUID().toString();
        }

        @Override
        public Set<String> getGroups() {
            return Set.of("OPERADOR");
        }

        @Override
        public Set<String> getClaimNames() {
            return Set.of("tenant_id");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getClaim(String claimName) {
            if ("tenant_id".equals(claimName)) {
                return (T) tenantId.toString();
            }
            return null;
        }
    }

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
            if (tenantId == null || codigo == null) return Optional.empty();
            String c = codigo.trim();
            return banco.values().stream()
                .filter(p -> p.getTenantId().valor().equals(tenantId))
                .filter(Produto::isAtivo)
                .filter(p -> c.equalsIgnoreCase(p.getCodigoBarras()) || c.equalsIgnoreCase(p.getCodigoInterno()))
                .findFirst();
        }

        @Override
        public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
            return Collections.emptyList();
        }

        @Override
        public List<Produto> listarAtivos(TenantId tenantId) {
            return banco.values().stream().filter(Produto::isAtivo).toList();
        }
    }
}
