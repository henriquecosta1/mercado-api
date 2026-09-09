package com.mercado.application.usecase;

import com.mercado.domain.entity.Tenant;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Gestão Administrativa de Licenças e Mensalidades de Tenants.
 * Permite ativar/renovar o acesso por X dias ou suspender estabelecimentos.
 */
@ApplicationScoped
public class GerenciarLicencaTenantUseCase {

    private final TenantRepository tenantRepository;

    @Inject
    public GerenciarLicencaTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository é obrigatório.");
    }

    @Transactional
    public Tenant ativar(UUID tenantIdUuid, int diasValidade) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("Identificador do tenant é obrigatório.");
        }
        int dias = diasValidade > 0 ? diasValidade : 30;

        TenantId tenantId = TenantId.de(tenantIdUuid);
        Tenant tenant = tenantRepository.buscarPorId(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado com id: " + tenantIdUuid));

        tenant.ativarPorDias(dias);
        tenantRepository.atualizar(tenant);
        return tenant;
    }

    @Transactional
    public Tenant suspender(UUID tenantIdUuid) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("Identificador do tenant é obrigatório.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);
        Tenant tenant = tenantRepository.buscarPorId(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado com id: " + tenantIdUuid));

        tenant.suspender();
        tenantRepository.atualizar(tenant);
        return tenant;
    }

    public List<Tenant> listarTodos() {
        return tenantRepository.listarTodos();
    }
}
