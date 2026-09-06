package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.Instant;
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
    private final Instant abertoEm;
    private Instant fechadoEm;

    public Caixa(UUID id,
                 TenantId tenantId,
                 Dinheiro saldoInicial,
                 Dinheiro saldoDinheiro,
                 StatusCaixa status,
                 Instant abertoEm,
                 Instant fechadoEm) {
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
        Instant agora = Instant.now();
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

    public void estornarVendaDinheiro(Dinheiro valor) {
        validarCaixaAberto();
        Objects.requireNonNull(valor, "Valor para estorno não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor para estorno deve ser estritamente positivo.");
        }
        if (this.saldoDinheiro.isMenorQue(valor)) {
            throw new RegraDeNegocioException(
                "Saldo em dinheiro insuficiente no caixa para realizar o estorno. Saldo disponível: " +
                this.saldoDinheiro + ", valor solicitado: " + valor
            );
        }
        this.saldoDinheiro = this.saldoDinheiro.subtrair(valor);
    }

    public void realizarSangria(Dinheiro valor, String motivo) {
        validarCaixaAberto();
        Objects.requireNonNull(valor, "Valor para sangria não pode ser nulo.");
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo da sangria é obrigatório.");
        }
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

    public void realizarSuprimento(Dinheiro valor, String motivo) {
        validarCaixaAberto();
        Objects.requireNonNull(valor, "Valor para suprimento não pode ser nulo.");
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do suprimento é obrigatório.");
        }
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor para suprimento deve ser estritamente positivo.");
        }
        this.saldoDinheiro = this.saldoDinheiro.somar(valor);
    }

    public void sangria(Dinheiro valor) {
        realizarSangria(valor, "Sangria avulsa");
    }

    public void fechar(Instant agora) {
        validarCaixaAberto();
        this.status = StatusCaixa.FECHADO;
        this.fechadoEm = Objects.requireNonNull(agora, "Data/hora de fechamento não pode ser nula.");
    }

    public void fechar() {
        fechar(Instant.now());
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

    public Instant getAbertoEm() {
        return abertoEm;
    }

    public Instant getFechadoEm() {
        return fechadoEm;
    }
}
