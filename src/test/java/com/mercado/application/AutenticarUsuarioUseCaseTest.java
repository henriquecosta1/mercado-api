package com.mercado.application;

import com.mercado.application.dto.LoginInput;
import com.mercado.application.dto.LoginOutput;
import com.mercado.application.usecase.AutenticarUsuarioUseCase;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: AutenticarUsuarioUseCase (Login)")
class AutenticarUsuarioUseCaseTest {

    private FakeUsuarioRepository usuarioRepository;
    private FakeTenantRepository tenantRepository;
    private AutenticarUsuarioUseCase useCase;

    private final TenantId tenant1 = TenantId.de(UUID.randomUUID());
    private final TenantId tenant2 = TenantId.de(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        usuarioRepository = new FakeUsuarioRepository();
        tenantRepository = new FakeTenantRepository();
        useCase = new AutenticarUsuarioUseCase(usuarioRepository, tenantRepository, "mercado-api");

        // Tenants
        Tenant t1 = new Tenant(tenant1, "Mercado Central", com.mercado.domain.valueobject.PinGerente.criar("1234"));
        tenantRepository.salvar(t1);

        Tenant t2 = new Tenant(tenant2, "Supermercado Progresso", com.mercado.domain.valueobject.PinGerente.criar("1234"));
        tenantRepository.salvar(t2);

        // Usuário no Tenant 1
        Usuario u1 = Usuario.criar(tenant1, "Roberio Gerente", "roberio", "12345678", Usuario.Perfil.GERENTE);
        usuarioRepository.salvar(u1);

        // Usuário no Tenant 2
        Usuario u2 = Usuario.criar(tenant2, "Daniel Operador", "daniel", "12345678", Usuario.Perfil.OPERADOR);
        usuarioRepository.salvar(u2);
    }

    @Test
    @DisplayName("Deve autenticar roberio sem informar tenantId e identificar o tenantId e nomeMercado corretos")
    void deveAutenticarRoberioSemTenantId() {
        LoginInput input = new LoginInput("roberio", "12345678");
        LoginOutput output = useCase.executar(input);

        assertNotNull(output);
        assertNotNull(output.token());
        assertEquals("Roberio Gerente", output.nome());
        assertEquals("GERENTE", output.perfil());
        assertEquals(tenant1.valor(), output.tenantId());
        assertEquals("Mercado Central", output.nomeMercado());
    }

    @Test
    @DisplayName("Deve autenticar daniel sem informar tenantId e identificar o tenantId e nomeMercado corretos")
    void deveAutenticarDanielSemTenantId() {
        LoginInput input = new LoginInput("daniel", "12345678");
        LoginOutput output = useCase.executar(input);

        assertNotNull(output);
        assertNotNull(output.token());
        assertEquals("Daniel Operador", output.nome());
        assertEquals("OPERADOR", output.perfil());
        assertEquals(tenant2.valor(), output.tenantId());
        assertEquals("Supermercado Progresso", output.nomeMercado());
    }

    @Test
    @DisplayName("Deve falhar com senha incorreta")
    void deveFalharComSenhaIncorreta() {
        LoginInput input = new LoginInput("roberio", "senhaErrada");
        assertThrows(io.quarkus.security.UnauthorizedException.class, () -> useCase.executar(input));
    }

    @Test
    @DisplayName("Deve falhar com usuário inexistente")
    void deveFalharComUsuarioInexistente() {
        LoginInput input = new LoginInput("naoexiste", "12345678");
        assertThrows(io.quarkus.security.UnauthorizedException.class, () -> useCase.executar(input));
    }

    @Test
    @DisplayName("Deve bloquear login quando tenant estiver com status PENDENTE")
    void deveBloquearLoginTenantPendente() {
        TenantId tenantPendenteId = TenantId.de(UUID.randomUUID());
        Tenant tPendente = Tenant.autoCadastrar("Padaria Nova", "11999998888", "1234");
        tenantRepository.salvar(tPendente);

        Usuario userPendente = Usuario.criar(tPendente.getId(), "Jose", "jose.padaria", "12345678", Usuario.Perfil.GERENTE);
        usuarioRepository.salvar(userPendente);

        LoginInput input = new LoginInput("jose.padaria", "12345678");
        com.mercado.domain.exception.AcessoTenantBloqueadoException ex = assertThrows(
            com.mercado.domain.exception.AcessoTenantBloqueadoException.class,
            () -> useCase.executar(input)
        );

        assertEquals("TENANT_PENDENTE", ex.getCodigo());
        assertTrue(ex.getMensagem().contains("aguardando liberação"));
    }

    @Test
    @DisplayName("Deve bloquear login quando tenant estiver BLOQUEADO ou VENCIDO")
    void deveBloquearLoginTenantVencido() {
        TenantId tenantVencidoId = TenantId.de(UUID.randomUUID());
        Tenant tVencido = new Tenant(tenantVencidoId, "Mercado Vencido", "11888887777", com.mercado.domain.entity.StatusTenant.ATIVO, java.time.Instant.now().minus(java.time.Duration.ofDays(1)), com.mercado.domain.valueobject.PinGerente.criar("1234"));
        tenantRepository.salvar(tVencido);

        Usuario userVencido = Usuario.criar(tVencido.getId(), "Mario", "mario.vencido", "12345678", Usuario.Perfil.GERENTE);
        usuarioRepository.salvar(userVencido);

        LoginInput input = new LoginInput("mario.vencido", "12345678");
        com.mercado.domain.exception.AcessoTenantBloqueadoException ex = assertThrows(
            com.mercado.domain.exception.AcessoTenantBloqueadoException.class,
            () -> useCase.executar(input)
        );

        assertEquals("TENANT_VENCIDO", ex.getCodigo());
        assertTrue(ex.getMensagem().contains("Mensalidade do sistema expirada"));
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
        public java.util.Optional<Usuario> buscarPorLogin(String login) { return store.values().stream().filter(u -> u.getLogin().equals(login)).findFirst(); }

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
}
