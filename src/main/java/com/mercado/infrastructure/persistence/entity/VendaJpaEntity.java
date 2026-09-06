package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.StatusVenda;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "cliente_id")
    public UUID clienteId;

    @Column(name = "nome_cliente", length = 120)
    public String nomeCliente;

    @Column(name = "motivo_cancelamento", length = 255)
    public String motivoCancelamento;

    @Column(name = "cancelada_em")
    public Instant canceladaEm;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

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
        entity.status = domain.getStatus() != null ? domain.getStatus().name() : StatusVenda.CONCLUIDA.name();
        entity.clienteId = domain.getClienteId();
        entity.nomeCliente = domain.getNomeCliente();
        entity.motivoCancelamento = domain.getMotivoCancelamento();
        entity.canceladaEm = domain.getCanceladaEm();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public void updateFromDomain(Venda domain) {
        this.status = domain.getStatus().name();
        this.motivoCancelamento = domain.getMotivoCancelamento();
        this.canceladaEm = domain.getCanceladaEm();
    }

    public Venda toDomain() {
        return toDomain(Collections.emptyList());
    }

    public Venda toDomain(List<ItemVenda> itens) {
        return new Venda(
            this.id,
            TenantId.de(this.tenantId),
            this.caixaId,
            Dinheiro.de(this.valorTotal),
            FormaPagamento.valueOf(this.formaPagamento),
            Dinheiro.de(this.troco),
            this.descricao,
            itens != null ? itens : Collections.emptyList(),
            StatusVenda.valueOf(this.status != null ? this.status : StatusVenda.CONCLUIDA.name()),
            this.clienteId,
            this.nomeCliente,
            this.motivoCancelamento,
            this.canceladaEm,
            this.criadoEm
        );
    }
}
