package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categorias_produto")
public class CategoriaProdutoJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "nome", nullable = false, length = 60)
    public String nome;

    @Column(name = "icone", length = 30)
    public String icone;

    @Column(name = "ativo", nullable = false)
    public boolean ativo;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public CategoriaProdutoJpaEntity() {
    }

    public static CategoriaProdutoJpaEntity fromDomain(CategoriaProduto domain) {
        CategoriaProdutoJpaEntity entity = new CategoriaProdutoJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.nome = domain.getNome();
        entity.icone = domain.getIcone();
        entity.ativo = domain.isAtivo();
        entity.criadoEm = domain.getCriadoEm() != null ? domain.getCriadoEm() : Instant.now();
        return entity;
    }

    public void updateFromDomain(CategoriaProduto domain) {
        this.nome = domain.getNome();
        this.icone = domain.getIcone();
        this.ativo = domain.isAtivo();
    }

    public CategoriaProduto toDomain() {
        return new CategoriaProduto(
            this.id,
            TenantId.de(this.tenantId),
            this.nome,
            this.icone,
            this.ativo,
            this.criadoEm
        );
    }
}
