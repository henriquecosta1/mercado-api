package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.StatusCliente;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ClienteJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ClienteRepositoryAdapter implements ClienteRepository, PanacheRepositoryBase<ClienteJpaEntity, UUID> {

    @Override
    public Optional<Cliente> buscarPorId(TenantId tenantId, UUID id) {
        return find("tenantId = ?1 and id = ?2", tenantId.valor(), id)
            .firstResultOptional()
            .map(ClienteJpaEntity::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorNomeOuTelefone(TenantId tenantId, String nome, String telefone) {
        if (telefone != null && !telefone.isBlank()) {
            Optional<Cliente> porTelefone = find("tenantId = ?1 and telefone = ?2", tenantId.valor(), telefone.trim())
                .firstResultOptional()
                .map(ClienteJpaEntity::toDomain);
            if (porTelefone.isPresent()) {
                return porTelefone;
            }
        }

        return find("tenantId = ?1 and lower(nome) = lower(?2)", tenantId.valor(), nome.trim())
            .firstResultOptional()
            .map(ClienteJpaEntity::toDomain);
    }

    @Override
    public List<Cliente> listarComSaldoDevedor(TenantId tenantId) {
        return find("tenantId = ?1 and saldoDevedor > 0 order by nome asc", tenantId.valor())
            .list()
            .stream()
            .map(ClienteJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Cliente> buscarPorNome(String nome, TenantId tenantId) {
        if (nome == null || nome.isBlank()) {
            return find("tenantId = ?1 order by nome asc", tenantId.valor())
                .list()
                .stream()
                .map(ClienteJpaEntity::toDomain)
                .toList();
        }
        return find("tenantId = ?1 and (lower(nome) like lower(?2) or (apelido is not null and lower(apelido) like lower(?2)) or (telefone is not null and telefone like ?2)) order by nome asc",
                    tenantId.valor(), "%" + nome.trim() + "%")
            .list()
            .stream()
            .map(ClienteJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Cliente> listarTodos(TenantId tenantId, String busca, String status, Boolean apenasDevedores) {
        StringBuilder query = new StringBuilder("tenantId = :tenantId");
        Parameters params = Parameters.with("tenantId", tenantId.valor());

        if (busca != null && !busca.isBlank()) {
            query.append(" and (lower(nome) like :busca or (apelido is not null and lower(apelido) like :busca) or (telefone is not null and telefone like :busca) or (cpf is not null and cpf like :busca))");
            params.and("busca", "%" + busca.trim().toLowerCase() + "%");
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("TODOS")) {
            query.append(" and status = :status");
            params.and("status", StatusCliente.de(status));
        }

        if (Boolean.TRUE.equals(apenasDevedores)) {
            query.append(" and saldoDevedor > 0");
        }

        query.append(" order by nome asc");

        return find(query.toString(), params)
            .list()
            .stream()
            .map(ClienteJpaEntity::toDomain)
            .toList();
    }

    @Override
    public com.mercado.domain.repository.PageResult<Cliente> listarTodosPaginado(TenantId tenantId, String busca, String status, Boolean apenasDevedores, int page, int size) {
        StringBuilder query = new StringBuilder("tenantId = :tenantId");
        Parameters params = Parameters.with("tenantId", tenantId.valor());

        if (busca != null && !busca.isBlank()) {
            query.append(" and (lower(nome) like :busca or (apelido is not null and lower(apelido) like :busca) or (telefone is not null and telefone like :busca) or (cpf is not null and cpf like :busca))");
            params.and("busca", "%" + busca.trim().toLowerCase() + "%");
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("TODOS")) {
            query.append(" and status = :status");
            params.and("status", StatusCliente.de(status));
        }

        if (Boolean.TRUE.equals(apenasDevedores)) {
            query.append(" and saldoDevedor > 0");
        }

        query.append(" order by nome asc");

        var panacheQuery = find(query.toString(), params);
        long totalElements = panacheQuery.count();

        List<Cliente> content = panacheQuery.page(io.quarkus.panache.common.Page.of(page, size))
            .list()
            .stream()
            .map(ClienteJpaEntity::toDomain)
            .toList();

        return new com.mercado.domain.repository.PageResult<>(content, page, size, totalElements);
    }

    @Override
    public void excluir(UUID id, TenantId tenantId) {
        delete("tenantId = ?1 and id = ?2", tenantId.valor(), id);
    }

    @Override
    public BigDecimal somarTotalSaldoDevedor(TenantId tenantId) {
        BigDecimal total = find("select coalesce(sum(c.saldoDevedor), 0) from ClienteJpaEntity c where c.tenantId = ?1 and c.saldoDevedor > 0", tenantId.valor())
            .project(BigDecimal.class)
            .firstResult();
        return total != null ? total.setScale(2, java.math.RoundingMode.HALF_EVEN) : java.math.BigDecimal.ZERO.setScale(2);
    }

    @Override
    public void salvar(Cliente cliente) {
        persist(ClienteJpaEntity.fromDomain(cliente));
    }

    @Override
    public void atualizar(Cliente cliente) {
        ClienteJpaEntity jpaEntity = findById(cliente.getId());
        if (jpaEntity != null) {
            jpaEntity.updateFromDomain(cliente);
        } else {
            getEntityManager().merge(ClienteJpaEntity.fromDomain(cliente));
        }
    }
}