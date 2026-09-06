package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<ItemVenda> itens;
    private StatusVenda status;
    private final UUID clienteId;
    private final String nomeCliente;
    private String motivoCancelamento;
    private Instant canceladaEm;
    private final Instant criadoEm;

    public Venda(UUID id,
                 TenantId tenantId,
                 UUID caixaId,
                 Dinheiro valorTotal,
                 FormaPagamento formaPagamento,
                 Dinheiro troco,
                 String descricao,
                 List<ItemVenda> itens,
                 StatusVenda status,
                 UUID clienteId,
                 String nomeCliente,
                 String motivoCancelamento,
                 Instant canceladaEm,
                 Instant criadoEm) {
        this.id = Objects.requireNonNull(id, "Id da venda não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.caixaId = Objects.requireNonNull(caixaId, "CaixaId não pode ser nulo.");
        this.valorTotal = Objects.requireNonNull(valorTotal, "Valor total não pode ser nulo.");
        this.formaPagamento = Objects.requireNonNull(formaPagamento, "Forma de pagamento não pode ser nula.");
        this.troco = Objects.requireNonNull(troco, "Troco não pode ser nulo.");
        this.descricao = descricao;
        this.itens = itens != null ? new ArrayList<>(itens) : new ArrayList<>();
        this.status = status != null ? status : StatusVenda.CONCLUIDA;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação da venda não pode ser nula.");
    }

    public Venda(UUID id,
                 TenantId tenantId,
                 UUID caixaId,
                 Dinheiro valorTotal,
                 FormaPagamento formaPagamento,
                 Dinheiro troco,
                 String descricao,
                 List<ItemVenda> itens,
                 Instant criadoEm) {
        this(id, tenantId, caixaId, valorTotal, formaPagamento, troco, descricao, itens, StatusVenda.CONCLUIDA, null, null, null, null, criadoEm);
    }

    public Venda(UUID id,
                 TenantId tenantId,
                 UUID caixaId,
                 Dinheiro valorTotal,
                 FormaPagamento formaPagamento,
                 Dinheiro troco,
                 String descricao,
                 Instant criadoEm) {
        this(id, tenantId, caixaId, valorTotal, formaPagamento, troco, descricao, Collections.emptyList(), StatusVenda.CONCLUIDA, null, null, null, null, criadoEm);
    }

    public static Venda criar(TenantId tenantId,
                              UUID caixaId,
                              Dinheiro valorTotal,
                              FormaPagamento formaPagamento,
                              Dinheiro troco,
                              String descricao,
                              List<ItemVenda> itens,
                              UUID clienteId,
                              String nomeCliente) {
        Objects.requireNonNull(valorTotal, "Valor total da venda não pode ser nulo.");
        if (valorTotal.isNegativo() || valorTotal.isZero()) {
            throw new RegraDeNegocioException("Valor total da venda deve ser maior que zero.");
        }
        Objects.requireNonNull(troco, "Troco não pode ser nulo.");
        if (troco.isNegativo()) {
            throw new RegraDeNegocioException("Troco não pode ser negativo.");
        }

        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();

        return new Venda(
            id,
            tenantId,
            caixaId,
            valorTotal,
            formaPagamento,
            troco,
            descricao,
            itens != null ? itens : Collections.emptyList(),
            StatusVenda.CONCLUIDA,
            clienteId,
            nomeCliente,
            null,
            null,
            agora
        );
    }

    public static Venda criar(TenantId tenantId,
                              UUID caixaId,
                              Dinheiro valorTotal,
                              FormaPagamento formaPagamento,
                              Dinheiro troco,
                              String descricao,
                              List<ItemVenda> itens) {
        return criar(tenantId, caixaId, valorTotal, formaPagamento, troco, descricao, itens, null, null);
    }

    public static Venda criar(TenantId tenantId,
                              UUID caixaId,
                              Dinheiro valorTotal,
                              FormaPagamento formaPagamento,
                              Dinheiro troco,
                              String descricao) {
        return criar(tenantId, caixaId, valorTotal, formaPagamento, troco, descricao, Collections.emptyList(), null, null);
    }

    public void cancelar(String motivo) {
        if (this.status == StatusVenda.CANCELADA) {
            throw new RegraDeNegocioException("Venda já se encontra cancelada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório.");
        }
        this.status = StatusVenda.CANCELADA;
        this.motivoCancelamento = motivo.trim();
        this.canceladaEm = Instant.now();
    }

    public boolean isCancelada() {
        return this.status == StatusVenda.CANCELADA;
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

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public StatusVenda getStatus() {
        return status;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
