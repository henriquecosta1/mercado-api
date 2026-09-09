package com.mercado.domain.repository;

import com.mercado.domain.entity.Tenant;
import com.mercado.domain.valueobject.TenantId;

import java.util.Optional;

/**
 * Interface pura de repositório para a entidade Tenant (DIP - Dependency Inversion Principle).
 */
public interface TenantRepository {

    Optional<Tenant> buscarPorId(TenantId id);

    void salvar(Tenant tenant);

    void atualizar(Tenant tenant);
}
