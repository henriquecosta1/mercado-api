package com.mercado.domain.repository;

import com.mercado.domain.entity.Caixa;
import com.mercado.domain.valueobject.TenantId;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface pura de repositório para a entidade Caixa (DIP - Dependency Inversion Principle).
 * Livre de qualquer acoplamento com frameworks ou JPA.
 */
public interface CaixaRepository {

    Optional<Caixa> buscarCaixaAberto(TenantId tenantId);

    default Optional<Caixa> buscarAberto(TenantId tenantId) {
        return buscarCaixaAberto(tenantId);
    }

    Optional<Caixa> buscarPorId(TenantId tenantId, UUID id);

    default Optional<Caixa> buscarPorId(UUID id, TenantId tenantId) {
        return buscarPorId(tenantId, id);
    }

    void salvar(Caixa caixa);

    void atualizar(Caixa caixa);
}
