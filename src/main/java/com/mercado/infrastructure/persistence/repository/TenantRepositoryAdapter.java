package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Tenant;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.TenantJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantRepositoryAdapter implements TenantRepository, PanacheRepositoryBase<TenantJpaEntity, UUID> {

    @Override
    public Optional<Tenant> buscarPorId(TenantId id) {
        return findByIdOptional(id.valor())
            .map(TenantJpaEntity::toDomain);
    }

    @Override
    public java.util.List<Tenant> listarTodos() {
        return listAll().stream()
            .map(TenantJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void salvar(Tenant tenant) {
        persist(TenantJpaEntity.fromDomain(tenant));
    }

    @Override
    public void atualizar(Tenant tenant) {
        TenantJpaEntity entity = findById(tenant.getId().valor());
        if (entity != null) {
            entity.updateFromDomain(tenant);
        } else {
            persist(TenantJpaEntity.fromDomain(tenant));
        }
    }
}
