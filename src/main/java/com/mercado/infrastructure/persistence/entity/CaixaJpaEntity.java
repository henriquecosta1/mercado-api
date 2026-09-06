package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.StatusCaixa;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "caixas")
public class CaixaJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "saldo_inicial", nullable = false, precision = 12, scale = 2)
    public BigDecimal saldoInicial;

    @Column(name = "saldo_dinheiro", nullable = false, precision = 12, scale = 2)
    public BigDecimal saldoDinheiro;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "aberto_em", nullable = false)
    public Instant abertoEm;

    @Column(name = "fechado_em")
    public Instant fechadoEm;

    public CaixaJpaEntity() {
    }

    public static CaixaJpaEntity fromDomain(Caixa domain) {
        CaixaJpaEntity entity = new CaixaJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.saldoInicial = domain.getSaldoInicial().valor();
        entity.saldoDinheiro = domain.getSaldoDinheiro().valor();
        entity.status = domain.getStatus().name();
        entity.abertoEm = domain.getAbertoEm();
        entity.fechadoEm = domain.getFechadoEm();
        return entity;
    }

    public void updateFromDomain(Caixa domain) {
        this.saldoDinheiro = domain.getSaldoDinheiro().valor();
        this.status = domain.getStatus().name();
        this.fechadoEm = domain.getFechadoEm();
    }

    public Caixa toDomain() {
        return new Caixa(
            this.id,
            TenantId.de(this.tenantId),
            Dinheiro.de(this.saldoInicial),
            Dinheiro.de(this.saldoDinheiro),
            StatusCaixa.valueOf(this.status),
            this.abertoEm,
            this.fechadoEm
        );
    }
}
