package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.repository.MovimentacaoCaixaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.MovimentacaoCaixaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentacaoCaixaRepositoryAdapter implements MovimentacaoCaixaRepository, PanacheRepositoryBase<MovimentacaoCaixaJpaEntity, UUID> {

    @Override
    public void salvar(MovimentacaoCaixa mov) {
        persist(MovimentacaoCaixaJpaEntity.fromDomain(mov));
    }

    @Override
    public List<MovimentacaoCaixa> listarPorCaixa(UUID caixaId, TenantId tenantId) {
        return find("tenantId = ?1 and caixaId = ?2 order by criadoEm asc", tenantId.valor(), caixaId)
            .list()
            .stream()
            .map(MovimentacaoCaixaJpaEntity::toDomain)
            .toList();
    }
}
