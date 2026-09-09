package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Amortizacao;
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
@Table(name = "amortizacoes")
public class AmortizacaoJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "cliente_id", nullable = false)
    public UUID clienteId;

    @Column(name = "caixa_id", nullable = false)
    public UUID caixaId;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    public BigDecimal valor;

    @Column(name = "forma_pagamento", nullable = false, length = 20)
    public String formaPagamento;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public AmortizacaoJpaEntity() {
    }

    public static AmortizacaoJpaEntity fromDomain(Amortizacao domain) {
        AmortizacaoJpaEntity entity = new AmortizacaoJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.clienteId = domain.getClienteId();
        entity.caixaId = domain.getCaixaId();
        entity.valor = domain.getValor().valor();
        entity.formaPagamento = domain.getFormaPagamento();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public Amortizacao toDomain() {
        return new Amortizacao(
            this.id,
            TenantId.de(this.tenantId),
            this.clienteId,
            this.caixaId,
            Dinheiro.de(this.valor),
            this.formaPagamento,
            this.criadoEm
        );
    }
}
