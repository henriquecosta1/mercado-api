package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.VendaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VendaRepositoryAdapter implements VendaRepository, PanacheRepositoryBase<VendaJpaEntity, UUID> {

    @Override
    public void salvar(Venda venda) {
        persist(VendaJpaEntity.fromDomain(venda));
    }

    @Override
    public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
        return find("tenantId = ?1 and id = ?2", tenantId.valor(), id)
            .firstResultOptional()
            .map(VendaJpaEntity::toDomain);
    }
}
