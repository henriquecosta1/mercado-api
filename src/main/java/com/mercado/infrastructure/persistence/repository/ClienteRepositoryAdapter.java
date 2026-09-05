package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ClienteJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

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
            return listarComSaldoDevedor(tenantId);
        }
        return find("tenantId = ?1 and lower(nome) like lower(?2) order by nome asc",
                    tenantId.valor(), "%" + nome.trim() + "%")
            .list()
            .stream()
            .map(ClienteJpaEntity::toDomain)
            .toList();
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
