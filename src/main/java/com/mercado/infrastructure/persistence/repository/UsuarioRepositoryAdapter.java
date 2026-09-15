package com.mercado.infrastructure.persistence.repository;

import com.mercado.domain.entity.Usuario;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.UsuarioJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementacao do UsuarioRepository usando Panache + PostgreSQL.
 * Segue o padrao Adapter (Ports and Adapters) da Clean Architecture.
 */
@ApplicationScoped
public class UsuarioRepositoryAdapter implements UsuarioRepository, PanacheRepositoryBase<UsuarioJpaEntity, UUID> {

    @Override
    public Optional<Usuario> buscarPorLogin(String login, TenantId tenantId) {
        return find("login = ?1 and tenantId = ?2 and ativo = true",
                login.toLowerCase(), tenantId.valor())
            .firstResultOptional()
            .map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return find("login = ?1 and ativo = true", login.toLowerCase())
            .firstResultOptional()
            .map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id, TenantId tenantId) {
        return find("id = ?1 and tenantId = ?2", id, tenantId.valor())
            .firstResultOptional()
            .map(UsuarioJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void salvar(Usuario usuario) {
        UsuarioJpaEntity existing = findById(usuario.getId());
        if (existing != null) {
            existing.updateFromDomain(usuario);
        } else {
            persist(UsuarioJpaEntity.fromDomain(usuario));
        }
    }
}