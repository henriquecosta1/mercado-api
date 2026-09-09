package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.repository.CategoriaProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.CategoriaProdutoJpaEntity;
import com.mercado.infrastructure.persistence.entity.ProdutoJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistência para CategoriaProduto usando Panache + PostgreSQL.
 */
@ApplicationScoped
public class CategoriaProdutoRepositoryAdapter implements CategoriaProdutoRepository, PanacheRepositoryBase<CategoriaProdutoJpaEntity, UUID> {

    @Override
    public void salvar(CategoriaProduto categoria) {
        CategoriaProdutoJpaEntity existing = findById(categoria.getId());
        if (existing != null) {
            existing.updateFromDomain(categoria);
        } else {
            persist(CategoriaProdutoJpaEntity.fromDomain(categoria));
        }
    }

    @Override
    public List<CategoriaProduto> listarPorTenant(TenantId tenantId) {
        return find("tenantId = ?1 and ativo = true order by nome asc", tenantId.valor())
            .list()
            .stream()
            .map(CategoriaProdutoJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<CategoriaProduto> buscarPorId(UUID id, TenantId tenantId) {
        return find("tenantId = ?1 and id = ?2", tenantId.valor(), id)
            .firstResultOptional()
            .map(CategoriaProdutoJpaEntity::toDomain);
    }

    @Override
    public Optional<CategoriaProduto> buscarPorNome(String nome, TenantId tenantId) {
        if (nome == null || nome.isBlank()) {
            return Optional.empty();
        }
        return find("tenantId = ?1 and lower(nome) = lower(?2)", tenantId.valor(), nome.trim())
            .firstResultOptional()
            .map(CategoriaProdutoJpaEntity::toDomain);
    }

    @Override
    public boolean existeVinculoComProduto(UUID categoriaId, TenantId tenantId) {
        CategoriaProdutoJpaEntity categoriaEntity = findById(categoriaId);
        if (categoriaEntity == null) {
            return false;
        }
        long count = ProdutoJpaEntity.count("tenantId = ?1 and lower(categoria) = lower(?2)",
            tenantId.valor(), categoriaEntity.nome);
        return count > 0;
    }

    @Override
    public void excluir(UUID id, TenantId tenantId) {
        delete("tenantId = ?1 and id = ?2", tenantId.valor(), id);
    }
}
