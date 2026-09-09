package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Usuario;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "nome", nullable = false, length = 100)
    public String nome;

    @Column(name = "login", nullable = false, length = 50)
    public String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    public String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 20)
    public Usuario.Perfil perfil;

    @Column(name = "ativo", nullable = false)
    public boolean ativo;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public UsuarioJpaEntity() {
    }

    public static UsuarioJpaEntity fromDomain(Usuario domain) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.nome = domain.getNome();
        entity.login = domain.getLogin();
        entity.senhaHash = domain.getSenhaHash();
        entity.perfil = domain.getPerfil();
        entity.ativo = domain.isAtivo();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public void updateFromDomain(Usuario domain) {
        this.nome = domain.getNome();
        this.senhaHash = domain.getSenhaHash();
        this.perfil = domain.getPerfil();
        this.ativo = domain.isAtivo();
    }

    public Usuario toDomain() {
        return new Usuario(
            this.id,
            TenantId.de(this.tenantId),
            this.nome,
            this.login,
            this.senhaHash,
            this.perfil,
            this.ativo,
            this.criadoEm
        );
    }
}