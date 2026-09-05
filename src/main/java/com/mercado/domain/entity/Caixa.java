package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando o Caixa da loja.
 * Totalmente desacoplada de frameworks.
 */
public class Caixa {

    private final UUID id;
    private final TenantId tenantId;
    private final Dinheiro saldoInicial;
    private Dinheiro saldoDinheiro;
    private StatusCaixa status;
    private final LocalDateTime abertoEm;
    private LocalDateTime fechadoEm;

    public Caixa(UUID id,
                 TenantId tenantId,
                 Dinheiro saldoInicial,
                 Dinheiro saldoDinheiro,
                 StatusCaixa status,
                 LocalDateTime abertoEm,
                 LocalDateTime fechadoEm) {
        this.id = Objects.requireNonNull(id, "Id do caixa não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.saldoInicial = Objects.requireNonNull(saldoInicial, "Saldo inicial não pode ser nulo.");
        this.saldoDinheiro = Objects.requireNonNull(saldoDinheiro, "Saldo em dinheiro não pode ser nulo.");
        this.status = Objects.requireNonNull(status, "Status do caixa não pode ser nulo.");
        this.abertoEm = Objects.requireNonNull(abertoEm, "Data de abertura não pode ser nula.");
        this.fechadoEm = fechadoEm;
    }

    public static Caixa abrir(TenantId tenantId, Dinheiro saldoInicial) {
        Objects.requireNonNull(saldoInicial, "Saldo inicial não pode ser nulo.");
        if (saldoInicial.isNegativo()) {
            throw new RegraDeNegocioException("Saldo inicial do caixa não pode ser negativo.");
        }
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        return new Caixa(id, tenantId, saldoInicial, saldoInicial, StatusCaixa.ABERTO, agora, null);
    }

    public void adicionarDinheiro(Dinheiro valor) {
        validarCaixaAberto();
        Objects.requireNonNull(valor, "Valor a adicionar não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor a adicionar ao caixa deve ser estritamente positivo.");
        }
        this.saldoDinheiro = this.saldoDinheiro.somar(valor);
    }

    public void sangria(Dinheiro valor) {
        validarCaixaAberto();
        Objects.requireNonNull(valor, "Valor para sangria não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor para sangria deve ser estritamente positivo.");
        }
        if (this.saldoDinheiro.isMenorQue(valor)) {
            throw new RegraDeNegocioException(
                "Saldo insuficiente no caixa para efetuar a sangria. Saldo disponível: " +
                this.saldoDinheiro + ", valor solicitado: " + valor
            );
        }
        this.saldoDinheiro = this.saldoDinheiro.subtrair(valor);
    }

    public void fechar() {
        validarCaixaAberto();
        this.status = StatusCaixa.FECHADO;
        this.fechadoEm = LocalDateTime.now();
    }

    public boolean isAberto() {
        return this.status == StatusCaixa.ABERTO;
    }

    private void validarCaixaAberto() {
        if (this.status != StatusCaixa.ABERTO) {
            throw new RegraDeNegocioException("Operação inválida: o caixa está fechado.");
        }
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public Dinheiro getSaldoInicial() {
        return saldoInicial;
    }

    public Dinheiro getSaldoDinheiro() {
        return saldoDinheiro;
    }

    public StatusCaixa getStatus() {
        return status;
    }

    public LocalDateTime getAbertoEm() {
        return abertoEm;
    }

    public LocalDateTime getFechadoEm() {
        return fechadoEm;
    }
}
