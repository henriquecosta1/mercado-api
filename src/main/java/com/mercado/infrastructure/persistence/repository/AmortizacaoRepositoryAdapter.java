package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Amortizacao;
import com.mercado.domain.repository.AmortizacaoRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.AmortizacaoJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AmortizacaoRepositoryAdapter implements AmortizacaoRepository, PanacheRepositoryBase<AmortizacaoJpaEntity, UUID> {

    @Override
    public void salvar(Amortizacao amortizacao) {
        persist(AmortizacaoJpaEntity.fromDomain(amortizacao));
    }

    @Override
    public List<Amortizacao> listarPorCliente(UUID clienteId, TenantId tenantId) {
        return find("tenantId = ?1 and clienteId = ?2 order by criadoEm desc", tenantId.valor(), clienteId)
            .list()
            .stream()
            .map(AmortizacaoJpaEntity::toDomain)
            .toList();
    }
}
