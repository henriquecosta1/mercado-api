package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendas")
public class VendaJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "caixa_id", nullable = false)
    public UUID caixaId;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    public BigDecimal valorTotal;

    @Column(name = "forma_pagamento", nullable = false, length = 20)
    public String formaPagamento;

    @Column(name = "troco", nullable = false, precision = 12, scale = 2)
    public BigDecimal troco;

    @Column(name = "descricao", length = 255)
    public String descricao;

    @Column(name = "criado_em", nullable = false)
    public LocalDateTime criadoEm;

    public VendaJpaEntity() {
    }

    public static VendaJpaEntity fromDomain(Venda domain) {
        VendaJpaEntity entity = new VendaJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.caixaId = domain.getCaixaId();
        entity.valorTotal = domain.getValorTotal().valor();
        entity.formaPagamento = domain.getFormaPagamento().name();
        entity.troco = domain.getTroco().valor();
        entity.descricao = domain.getDescricao();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public Venda toDomain() {
        return new Venda(
            this.id,
            TenantId.de(this.tenantId),
            this.caixaId,
            Dinheiro.de(this.valorTotal),
            FormaPagamento.valueOf(this.formaPagamento),
            Dinheiro.de(this.troco),
            this.descricao,
            this.criadoEm
        );
    }
}
