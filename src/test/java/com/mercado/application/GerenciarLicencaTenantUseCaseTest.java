package com.mercado.application;

import com.mercado.application.usecase.GerenciarLicencaTenantUseCase;
import com.mercado.domain.entity.StatusTenant;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: GerenciarLicencaTenantUseCase")
class GerenciarLicencaTenantUseCaseTest {

    private FakeTenantRepository tenantRepository;
    private GerenciarLicencaTenantUseCase useCase;

    @BeforeEach
    void setUp() {
        tenantRepository = new FakeTenantRepository();
        useCase = new GerenciarLicencaTenantUseCase(tenantRepository);
    }

    @Test
    @DisplayName("Deve ativar tenant pendente definindo status ATIVO e data de expiração")
    void deveAtivarTenantPendente() {
        Tenant tenant = Tenant.autoCadastrar("Comércio São Jorge", "11999990000", "1234");
        tenantRepository.salvar(tenant);

        assertTrue(tenant.isPendente());
        assertNull(tenant.getDataExpiracaoLicenca());

        Tenant ativado = useCase.ativar(tenant.getId().valor(), 30);

        assertEquals(StatusTenant.ATIVO, ativado.getStatus());
        assertFalse(ativado.isPendente());
        assertTrue(ativado.isAtivo());
        assertNotNull(ativado.getDataExpiracaoLicenca());
        assertTrue(ativado.getDataExpiracaoLicenca().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Deve suspender tenant ativo")
    void deveSuspenderTenant() {
        Tenant tenant = Tenant.criar("Supermercado Central");
        tenantRepository.salvar(tenant);

        assertTrue(tenant.isAtivo());

        Tenant suspenso = useCase.suspender(tenant.getId().valor());

        assertEquals(StatusTenant.BLOQUEADO, suspenso.getStatus());
        assertFalse(suspenso.isAtivo());
        assertTrue(suspenso.isVencido());
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
