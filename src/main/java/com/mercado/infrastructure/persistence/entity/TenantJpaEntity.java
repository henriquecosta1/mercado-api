package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.StatusTenant;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.valueobject.PinGerente;
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
@Table(name = "tenants")
public class TenantJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    public String nome;

    @Column(name = "whatsapp", length = 30)
    public String whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusTenant status;

    @Column(name = "data_expiracao_licenca")
    public Instant dataExpiracaoLicenca;

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
        entity.whatsapp = domain.getWhatsapp();
        entity.status = domain.getStatus() != null ? domain.getStatus() : StatusTenant.ATIVO;
        entity.dataExpiracaoLicenca = domain.getDataExpiracaoLicenca();
        entity.pinGerente = domain.getPinGerente().getHash();
        entity.criadoEm = Instant.now();
        return entity;
    }

    public void updateFromDomain(Tenant domain) {
        this.nome = domain.getNome();
        this.whatsapp = domain.getWhatsapp();
        this.status = domain.getStatus() != null ? domain.getStatus() : StatusTenant.ATIVO;
        this.dataExpiracaoLicenca = domain.getDataExpiracaoLicenca();
        this.pinGerente = domain.getPinGerente().getHash();
    }

    public Tenant toDomain() {
        return new Tenant(
            TenantId.de(this.id),
            this.nome,
            this.whatsapp,
            this.status != null ? this.status : StatusTenant.ATIVO,
            this.dataExpiracaoLicenca,
            PinGerente.deHash(this.pinGerente)
        );
    }
}
