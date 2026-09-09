package com.mercado.application;

import com.mercado.application.dto.AtualizarCategoriaInput;
import com.mercado.application.dto.CategoriaDTO;
import com.mercado.application.dto.CriarCategoriaInput;
import com.mercado.application.usecase.GerenciarCategoriasUseCase;
import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CategoriaProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: GerenciarCategoriasUseCase")
class GerenciarCategoriasUseCaseTest {

    private FakeCategoriaProdutoRepository repository;
    private GerenciarCategoriasUseCase useCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        repository = new FakeCategoriaProdutoRepository();
        useCase = new GerenciarCategoriasUseCase(repository);
    }

    @Test
    @DisplayName("Deve listar categorias ativas do tenant")
    void deveListarCategoriasDoTenant() {
        CategoriaProduto c1 = CategoriaProduto.criar(tenantId, "Mercearia", "cart");
        CategoriaProduto c2 = CategoriaProduto.criar(tenantId, "Bebidas", "beer");
        repository.salvar(c1);
        repository.salvar(c2);

        List<CategoriaDTO> lista = useCase.listar(tenantIdRaw);

        assertEquals(2, lista.size());
        assertEquals("Bebidas", lista.get(0).nome());
        assertEquals("Mercearia", lista.get(1).nome());
    }

    @Test
    @DisplayName("Deve criar nova categoria com sucesso")
    void deveCriarNovaCategoria() {
        CriarCategoriaInput input = new CriarCategoriaInput(tenantIdRaw, "Pet Shop", "dog");
        CategoriaDTO criada = useCase.criar(input);

        assertNotNull(criada.id());
        assertEquals("Pet Shop", criada.nome());
        assertEquals("dog", criada.icone());
        assertTrue(criada.ativo());
    }

    @Test
    @DisplayName("Deve impedir criação de categoria duplicada no mesmo tenant")
    void deveImpedirCategoriaDuplicada() {
        useCase.criar(new CriarCategoriaInput(tenantIdRaw, "Doces", "candy"));

        assertThrows(RegraDeNegocioException.class,
            () -> useCase.criar(new CriarCategoriaInput(tenantIdRaw, "Doces", "candy")));
    }

    @Test
    @DisplayName("Deve atualizar nome e ícone de uma categoria existente")
    void deveAtualizarCategoria() {
        CategoriaDTO criada = useCase.criar(new CriarCategoriaInput(tenantIdRaw, "Frios", "cheese"));

        AtualizarCategoriaInput updateInput = new AtualizarCategoriaInput(tenantIdRaw, criada.id(), "Frios & Queijos", "cheese");
        CategoriaDTO atualizada = useCase.atualizar(updateInput);

        assertEquals(criada.id(), atualizada.id());
        assertEquals("Frios & Queijos", atualizada.nome());
    }

    @Test
    @DisplayName("Deve excluir categoria quando não houver produtos vinculados")
    void deveExcluirCategoriaSemProdutos() {
        CategoriaDTO criada = useCase.criar(new CriarCategoriaInput(tenantIdRaw, "Descartáveis", "trash"));

        assertDoesNotThrow(() -> useCase.excluir(tenantIdRaw, criada.id()));
        assertTrue(repository.buscarPorId(criada.id(), tenantId).isEmpty());
    }

    @Test
    @DisplayName("Deve impedir exclusão de categoria que possui produtos vinculados")
    void deveImpedirExclusaoQuandoHouverProdutosVinculados() {
        CategoriaDTO criada = useCase.criar(new CriarCategoriaInput(tenantIdRaw, "Limpeza", "sparkles"));
        repository.vincularProduto(criada.id());

        assertThrows(RegraDeNegocioException.class,
            () -> useCase.excluir(tenantIdRaw, criada.id()));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException para categoria inexistente")
    void deveLancarExcecaoParaCategoriaInexistente() {
        UUID idInexistente = UUID.randomUUID();
        assertThrows(RecursoNaoEncontradoException.class,
            () -> useCase.buscarPorId(tenantIdRaw, idInexistente));
    }

    static class FakeCategoriaProdutoRepository implements CategoriaProdutoRepository {
        private final Map<UUID, CategoriaProduto> store = new HashMap<>();
        private final Set<UUID> categoriasComProdutos = new HashSet<>();

        public void vincularProduto(UUID categoriaId) {
            categoriasComProdutos.add(categoriaId);
        }

        @Override
        public void salvar(CategoriaProduto categoria) {
            store.put(categoria.getId(), categoria);
        }

        @Override
        public List<CategoriaProduto> listarPorTenant(TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.isAtivo())
                .sorted(Comparator.comparing(CategoriaProduto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        }

        @Override
        public Optional<CategoriaProduto> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.ofNullable(store.get(id))
                .filter(c -> c.getTenantId().equals(tenantId));
        }

        @Override
        public Optional<CategoriaProduto> buscarPorNome(String nome, TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst();
        }

        @Override
        public boolean existeVinculoComProduto(UUID categoriaId, TenantId tenantId) {
            return categoriasComProdutos.contains(categoriaId);
        }

        @Override
        public void excluir(UUID id, TenantId tenantId) {
            store.remove(id);
        }
    }
}
