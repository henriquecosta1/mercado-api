package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando o registro de uma Venda efetuada.
 * Totalmente desacoplada de frameworks.
 */
public class Venda {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID caixaId;
    private final Dinheiro valorTotal;
    private final FormaPagamento formaPagamento;
    private final Dinheiro troco;
    private final String descricao;
    private final LocalDateTime criadoEm;

    public Venda(UUID id,
                 TenantId tenantId,
                 UUID caixaId,
                 Dinheiro valorTotal,
                 FormaPagamento formaPagamento,
                 Dinheiro troco,
                 String descricao,
                 LocalDateTime criadoEm) {
        this.id = Objects.requireNonNull(id, "Id da venda não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.caixaId = Objects.requireNonNull(caixaId, "CaixaId não pode ser nulo.");
        this.valorTotal = Objects.requireNonNull(valorTotal, "Valor total não pode ser nulo.");
        this.formaPagamento = Objects.requireNonNull(formaPagamento, "Forma de pagamento não pode ser nula.");
        this.troco = Objects.requireNonNull(troco, "Troco não pode ser nulo.");
        this.descricao = descricao;
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação da venda não pode ser nula.");
    }

    public static Venda criar(TenantId tenantId,
                              UUID caixaId,
                              Dinheiro valorTotal,
                              FormaPagamento formaPagamento,
                              Dinheiro troco,
                              String descricao) {
        Objects.requireNonNull(valorTotal, "Valor total da venda não pode ser nulo.");
        if (valorTotal.isNegativo() || valorTotal.isZero()) {
            throw new RegraDeNegocioException("Valor total da venda deve ser maior que zero.");
        }
        Objects.requireNonNull(troco, "Troco não pode ser nulo.");
        if (troco.isNegativo()) {
            throw new RegraDeNegocioException("Troco não pode ser negativo.");
        }

        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        return new Venda(
            id,
            tenantId,
            caixaId,
            valorTotal,
            formaPagamento,
            troco,
            descricao,
            agora
        );
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public UUID getCaixaId() {
        return caixaId;
    }

    public Dinheiro getValorTotal() {
        return valorTotal;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public Dinheiro getTroco() {
        return troco;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
