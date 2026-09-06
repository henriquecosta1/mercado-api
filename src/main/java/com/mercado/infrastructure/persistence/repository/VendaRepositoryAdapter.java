package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ItemVendaJpaEntity;
import com.mercado.infrastructure.persistence.entity.VendaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VendaRepositoryAdapter implements VendaRepository, PanacheRepositoryBase<VendaJpaEntity, UUID> {

    @Override
    public void salvar(Venda venda) {
        VendaJpaEntity jpaEntity = findById(venda.getId());
        if (jpaEntity != null) {
            jpaEntity.updateFromDomain(venda);
        } else {
            persist(VendaJpaEntity.fromDomain(venda));
            if (venda.getItens() != null && !venda.getItens().isEmpty()) {
                for (ItemVenda item : venda.getItens()) {
                    ItemVendaJpaEntity itemJpa = ItemVendaJpaEntity.fromDomain(item, venda.getId());
                    itemJpa.persist();
                }
            }
        }
    }

    @Override
    public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
        return buscarPorIdComItens(id, tenantId);
    }

    @Override
    public Optional<Venda> buscarPorIdComItens(UUID vendaId, TenantId tenantId) {
        Optional<VendaJpaEntity> jpaOptional = find("tenantId = ?1 and id = ?2", tenantId.valor(), vendaId)
            .firstResultOptional();

        if (jpaOptional.isEmpty()) {
            return Optional.empty();
        }

        VendaJpaEntity vendaJpa = jpaOptional.get();
        List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaId).list();
        List<ItemVenda> itens = itensJpa.stream()
            .map(ItemVendaJpaEntity::toDomain)
            .toList();

        return Optional.of(vendaJpa.toDomain(itens));
    }

    @Override
    public List<Venda> listarPorCaixa(UUID caixaId, TenantId tenantId) {
        List<VendaJpaEntity> vendasJpa = find("tenantId = ?1 and caixaId = ?2 order by criadoEm desc", tenantId.valor(), caixaId)
            .list();

        return vendasJpa.stream()
            .map(vendaJpa -> {
                List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaJpa.id).list();
                List<ItemVenda> itens = itensJpa.stream()
                    .map(ItemVendaJpaEntity::toDomain)
                    .toList();
                return vendaJpa.toDomain(itens);
            })
            .toList();
    }

    @Override
    public List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
        return listarPorCaixa(caixaId, tenantId);
    }
}
