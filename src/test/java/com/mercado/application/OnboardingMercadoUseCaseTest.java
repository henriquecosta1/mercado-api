package com.mercado.application;

import com.mercado.application.dto.LoginOutput;
import com.mercado.application.dto.RegistrarMercadoInput;
import com.mercado.application.usecase.OnboardingMercadoUseCase;
import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CategoriaProdutoRepository;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: OnboardingMercadoUseCase")
class OnboardingMercadoUseCaseTest {

    private FakeTenantRepository tenantRepository;
    private FakeUsuarioRepository usuarioRepository;
    private FakeCategoriaProdutoRepository categoriaProdutoRepository;
    private OnboardingMercadoUseCase useCase;

    @BeforeEach
    void setUp() {
        tenantRepository = new FakeTenantRepository();
        usuarioRepository = new FakeUsuarioRepository();
        categoriaProdutoRepository = new FakeCategoriaProdutoRepository();
        useCase = new OnboardingMercadoUseCase(
            tenantRepository,
            usuarioRepository,
            categoriaProdutoRepository,
            "mercado-api"
        );
    }

    @Test
    @DisplayName("Deve registrar novo mercado criando tenant, admin, categorias padrão e gerando JWT")
    void deveRegistrarNovoMercadoComSucesso() {
        RegistrarMercadoInput input = new RegistrarMercadoInput(
            "Mercado Central",
            "Carlos Silva",
            "carlos.admin",
            "SenhaForte@123",
            "11988887777",
            "4321"
        );

        LoginOutput output = useCase.executar(input);

        assertNotNull(output);
        assertNotNull(output.token());
        assertEquals("Carlos Silva", output.nome());
        assertEquals("GERENTE", output.perfil());
        assertNotNull(output.tenantId());
        assertEquals("Mercado Central", output.nomeMercado());
        assertNotNull(output.expiraEm());

        // Verifica persistência do Tenant
        TenantId tenantId = TenantId.de(output.tenantId());
        Optional<Tenant> tenantOpt = tenantRepository.buscarPorId(tenantId);
        assertTrue(tenantOpt.isPresent());
        assertEquals("Mercado Central", tenantOpt.get().getNome());
        assertDoesNotThrow(() -> tenantOpt.get().validarPin("4321"));

        // Verifica persistência do Usuário com perfil GERENTE
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorLogin("carlos.admin", tenantId);
        assertTrue(usuarioOpt.isPresent());
        Usuario admin = usuarioOpt.get();
        assertEquals("Carlos Silva", admin.getNome());
        assertEquals(Usuario.Perfil.GERENTE, admin.getPerfil());
        assertTrue(admin.autenticar("SenhaForte@123"));

        // Verifica inserção das 7 categorias iniciais padrão
        List<CategoriaProduto> categorias = categoriaProdutoRepository.listarPorTenant(tenantId);
        assertEquals(7, categorias.size());
        List<String> nomesCategorias = categorias.stream().map(CategoriaProduto::getNome).toList();
        assertTrue(nomesCategorias.containsAll(List.of(
            "Mercearia", "Bebidas", "Frios & Laticínios", "Hortifrúti", "Padaria", "Limpeza", "Higiene"
        )));
    }

    @Test
    @DisplayName("Deve falhar ao registrar mercado com dados obrigatórios ausentes")
    void deveFalharComDadosInvalidos() {
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new RegistrarMercadoInput("", "Carlos", "admin", "123", "119999", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new RegistrarMercadoInput("Mercado", "", "admin", "123", "119999", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new RegistrarMercadoInput("Mercado", "Carlos", "", "123", "119999", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new RegistrarMercadoInput("Mercado", "Carlos", "admin", "", "119999", "1234")));
    }

    static class FakeTenantRepository implements TenantRepository {
        private final Map<TenantId, Tenant> store = new HashMap<>();

        @Override
        public Optional<Tenant> buscarPorId(TenantId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Tenant> listarTodos() {
            return List.copyOf(store.values());
        }

        @Override
        public void salvar(Tenant tenant) {
            store.put(tenant.getId(), tenant);
        }

        @Override
        public void atualizar(Tenant tenant) {
            store.put(tenant.getId(), tenant);
        }
    }

    static class FakeUsuarioRepository implements UsuarioRepository {
        private final Map<UUID, Usuario> store = new HashMap<>();

        @Override
        public Optional<Usuario> buscarPorLogin(String login, TenantId tenantId) {
            return store.values().stream()
                .filter(u -> u.getTenantId().equals(tenantId) && u.getLogin().equalsIgnoreCase(login.trim()))
                .findFirst();
        }

        @Override
        public List<Usuario> buscarPorLogin(String login) {
            return store.values().stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login.trim()))
                .toList();
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.ofNullable(store.get(id))
                .filter(u -> u.getTenantId().equals(tenantId));
        }

        @Override
        public void salvar(Usuario usuario) {
            store.put(usuario.getId(), usuario);
        }
    }

    static class FakeCategoriaProdutoRepository implements CategoriaProdutoRepository {
        private final Map<UUID, CategoriaProduto> store = new HashMap<>();

        @Override
        public void salvar(CategoriaProduto categoria) {
            store.put(categoria.getId(), categoria);
        }

        @Override
        public List<CategoriaProduto> listarPorTenant(TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId))
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
            return false;
        }

        @Override
        public void excluir(UUID id, TenantId tenantId) {
            store.remove(id);
        }
    }
}
