package com.mercado.application;

import com.mercado.application.dto.CadastroComercioInput;
import com.mercado.application.dto.CadastroComercioOutput;
import com.mercado.application.usecase.CadastrarComercioUseCase;
import com.mercado.domain.entity.StatusTenant;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: CadastrarComercioUseCase")
class CadastrarComercioUseCaseTest {

    private FakeTenantRepository tenantRepository;
    private FakeUsuarioRepository usuarioRepository;
    private CadastrarComercioUseCase useCase;

    @BeforeEach
    void setUp() {
        tenantRepository = new FakeTenantRepository();
        usuarioRepository = new FakeUsuarioRepository();
        useCase = new CadastrarComercioUseCase(tenantRepository, usuarioRepository);
    }

    @Test
    @DisplayName("Deve cadastrar novo comércio com status PENDENTE e criar o primeiro gerente")
    void deveCadastrarComercioComSucesso() {
        CadastroComercioInput input = new CadastroComercioInput(
            "Quitanda da Maria",
            "11988887777",
            "Maria Oliveira",
            "maria.quitanda",
            "Senha@123",
            "4321"
        );

        CadastroComercioOutput output = useCase.executar(input);

        assertNotNull(output);
        assertNotNull(output.tenantId());
        assertEquals("Quitanda da Maria", output.nomeComercio());
        assertEquals("PENDENTE", output.status());
        assertTrue(output.mensagem().contains("Aguarde a liberação"));

        // Valida Tenant no repositório
        TenantId tenantId = TenantId.de(output.tenantId());
        Optional<Tenant> tenantOpt = tenantRepository.buscarPorId(tenantId);
        assertTrue(tenantOpt.isPresent());
        Tenant tenant = tenantOpt.get();
        assertEquals("Quitanda da Maria", tenant.getNome());
        assertEquals("11988887777", tenant.getWhatsapp());
        assertEquals(StatusTenant.PENDENTE, tenant.getStatus());
        assertTrue(tenant.isPendente());
        assertFalse(tenant.isAtivo());

        // Valida Usuário Gerente criado
        Optional<Usuario> userOpt = usuarioRepository.buscarPorLogin("maria.quitanda", tenantId);
        assertTrue(userOpt.isPresent());
        Usuario gerente = userOpt.get();
        assertEquals("Maria Oliveira", gerente.getNome());
        assertEquals("maria.quitanda", gerente.getLogin());
        assertEquals(Usuario.Perfil.GERENTE, gerente.getPerfil());
        assertTrue(gerente.autenticar("Senha@123"));
    }

    @Test
    @DisplayName("Deve falhar ao tentar cadastrar comércio com login já existente")
    void deveFalharLoginDuplicado() {
        // Usuário existente
        TenantId t1 = TenantId.de(UUID.randomUUID());
        Usuario existente = Usuario.criar(t1, "Joao", "joao.admin", "123456", Usuario.Perfil.GERENTE);
        usuarioRepository.salvar(existente);

        CadastroComercioInput input = new CadastroComercioInput(
            "Mercadinho do Joao",
            "11977776666",
            "Joao Santos",
            "joao.admin",
            "Senha@123"
        );

        RegraDeNegocioException ex = assertThrows(
            RegraDeNegocioException.class,
            () -> useCase.executar(input)
        );
        assertTrue(ex.getMessage().contains("Login já está em uso"));
    }

    @Test
    @DisplayName("Deve falhar com campos obrigatórios ausentes")
    void deveFalharCamposObrigatorios() {
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new CadastroComercioInput("", "1199999", "Nome", "login", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new CadastroComercioInput("Nome", "", "Nome", "login", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new CadastroComercioInput("Nome", "1199999", "", "login", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new CadastroComercioInput("Nome", "1199999", "Nome", "", "1234")));
        assertThrows(RegraDeNegocioException.class,
            () -> useCase.executar(new CadastroComercioInput("Nome", "1199999", "Nome", "login", "12")));
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
}
