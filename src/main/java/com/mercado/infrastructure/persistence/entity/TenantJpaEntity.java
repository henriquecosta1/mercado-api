package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Tenant;
import com.mercado.domain.valueobject.PinGerente;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    public String nome;

    @Column(name = "pin_gerente", nullable = false, length = 100)
    public String pinGerente;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public TenantJpaEntity() {
    }

    public static TenantJpaEntity fromDomain(Tenant domain) {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.id = domain.getId().valor();
        entity.nome = domain.getNome();
        entity.pinGerente = domain.getPinGerente().getHash();
        entity.criadoEm = Instant.now();
        return entity;
    }

    public void updateFromDomain(Tenant domain) {
        this.nome = domain.getNome();
        this.pinGerente = domain.getPinGerente().getHash();
    }

    public Tenant toDomain() {
        return new Tenant(
            TenantId.de(this.id),
            this.nome,
            PinGerente.deHash(this.pinGerente)
        );
    }
}
