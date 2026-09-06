package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ProdutoJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
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

    @Override
    public List<Produto> listarGerencial(TenantId tenantId, String busca, String categoria, Boolean apenasEstoqueBaixo) {
        StringBuilder query = new StringBuilder("tenantId = :tenantId");
        Parameters params = Parameters.with("tenantId", tenantId.valor());

        if (busca != null && !busca.isBlank()) {
            query.append(" and lower(nome) like :busca");
            params.and("busca", "%" + busca.trim().toLowerCase() + "%");
        }

        if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todas")) {
            query.append(" and lower(categoria) = :categoria");
            params.and("categoria", categoria.trim().toLowerCase());
        }

        if (apenasEstoqueBaixo != null && apenasEstoqueBaixo) {
            query.append(" and estoqueAtual <= estoqueMinimo");
        }

        query.append(" order by nome asc");

        return find(query.toString(), params)
            .list()
            .stream()
            .map(ProdutoJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void excluirOuInativar(UUID id, TenantId tenantId) {
        ProdutoJpaEntity entity = find("tenantId = ?1 and id = ?2", tenantId.valor(), id).firstResult();
        if (entity != null) {
            entity.ativo = false;
        }
    }
}
