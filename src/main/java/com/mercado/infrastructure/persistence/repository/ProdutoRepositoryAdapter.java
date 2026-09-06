package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ProdutoJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutoRepositoryAdapter implements ProdutoRepository, PanacheRepositoryBase<ProdutoJpaEntity, UUID> {

    @Override
    public void salvar(Produto produto) {
        ProdutoJpaEntity jpaEntity = findById(produto.getId());
        if (jpaEntity != null) {
            jpaEntity.updateFromDomain(produto);
        } else {
            persist(ProdutoJpaEntity.fromDomain(produto));
        }
    }

    @Override
    public Optional<Produto> buscarPorId(UUID id, TenantId tenantId) {
        return find("tenantId = ?1 and id = ?2", tenantId.valor(), id)
            .firstResultOptional()
            .map(ProdutoJpaEntity::toDomain);
    }

    @Override
    public List<Produto> buscarPorNome(String termo, TenantId tenantId) {
        return find("tenantId = ?1 and ativo = true and lower(nome) like lower(?2) order by nome asc",
                    tenantId.valor(), "%" + termo.trim() + "%")
            .list()
            .stream()
            .map(ProdutoJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Produto> listarAtivos(TenantId tenantId) {
        return find("tenantId = ?1 and ativo = true order by nome asc", tenantId.valor())
            .list()
            .stream()
            .map(ProdutoJpaEntity::toDomain)
            .toList();
    }
}
