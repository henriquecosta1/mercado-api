package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.StatusCaixa;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.CaixaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CaixaRepositoryAdapter implements CaixaRepository, PanacheRepositoryBase<CaixaJpaEntity, UUID> {

    @Override
    public Optional<Caixa> buscarCaixaAberto(TenantId tenantId) {
        return find("tenantId = ?1 and status = ?2", tenantId.valor(), StatusCaixa.ABERTO.name())
            .firstResultOptional()
            .map(CaixaJpaEntity::toDomain);
    }

    @Override
    public Optional<Caixa> buscarPorId(TenantId tenantId, UUID id) {
        return find("tenantId = ?1 and id = ?2", tenantId.valor(), id)
            .firstResultOptional()
            .map(CaixaJpaEntity::toDomain);
    }

    @Override
    public void salvar(Caixa caixa) {
        persist(CaixaJpaEntity.fromDomain(caixa));
    }

    @Override
    public void atualizar(Caixa caixa) {
        CaixaJpaEntity jpaEntity = findById(caixa.getId());
        if (jpaEntity != null) {
            jpaEntity.updateFromDomain(caixa);
        } else {
            getEntityManager().merge(CaixaJpaEntity.fromDomain(caixa));
        }
    }
}
