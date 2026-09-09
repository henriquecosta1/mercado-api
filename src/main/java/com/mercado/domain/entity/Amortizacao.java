package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio representando o pagamento e amortização de fiado de um cliente.
 */
public class Amortizacao {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID clienteId;
    private final UUID caixaId;
    private final Dinheiro valor;
    private final String formaPagamento;
    private final Instant criadoEm;

    public Amortizacao(UUID id,
                       TenantId tenantId,
                       UUID clienteId,
                       UUID caixaId,
                       Dinheiro valor,
                       String formaPagamento,
                       Instant criadoEm) {
        this.id = Objects.requireNonNull(id, "Id da amortização não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.clienteId = Objects.requireNonNull(clienteId, "ClienteId não pode ser nulo.");
        this.caixaId = Objects.requireNonNull(caixaId, "CaixaId não pode ser nulo.");
        this.valor = Objects.requireNonNull(valor, "Valor da amortização não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor da amortização deve ser estritamente positivo.");
        }
        if (formaPagamento == null || formaPagamento.isBlank()) {
            throw new RegraDeNegocioException("Forma de pagamento da amortização é obrigatória.");
        }
        this.formaPagamento = formaPagamento.trim().toUpperCase();
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula.");
    }

    public static Amortizacao criar(TenantId tenantId,
                                    UUID clienteId,
                                    UUID caixaId,
                                    Dinheiro valor,
                                    String formaPagamento) {
        return new Amortizacao(
            UUID.randomUUID(),
            tenantId,
            clienteId,
            caixaId,
            valor,
            formaPagamento,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getCaixaId() {
        return caixaId;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
