package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando o Cliente da loja (com suporte à conta fiado).
 * Totalmente desacoplada de frameworks.
 */
public class Cliente {

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private String telefone;
    private Dinheiro limiteCredito;
    private Dinheiro saldoDevedor;
    private final LocalDateTime criadoEm;

    public Cliente(UUID id,
                   TenantId tenantId,
                   String nome,
                   String telefone,
                   Dinheiro limiteCredito,
                   Dinheiro saldoDevedor,
                   LocalDateTime criadoEm) {
        this.id = Objects.requireNonNull(id, "Id do cliente não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.nome = validarNome(nome);
        this.telefone = telefone;
        this.limiteCredito = Objects.requireNonNull(limiteCredito, "Limite de crédito não pode ser nulo.");
        this.saldoDevedor = Objects.requireNonNull(saldoDevedor, "Saldo devedor não pode ser nulo.");
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula.");
    }

    public static Cliente criar(TenantId tenantId, String nome, String telefone, Dinheiro limiteCredito) {
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        Dinheiro limite = limiteCredito != null ? limiteCredito : Dinheiro.zero();
        return new Cliente(id, tenantId, nome, telefone, limite, Dinheiro.zero(), agora);
    }

    public void registrarDebito(Dinheiro valor) {
        Objects.requireNonNull(valor, "Valor de débito não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor do débito fiado deve ser estritamente positivo.");
        }

        // Se houver limite de crédito configurado (maior que zero), valida o limite
        if (this.limiteCredito.isMaiorQue(Dinheiro.zero())) {
            Dinheiro novoSaldo = this.saldoDevedor.somar(valor);
            if (novoSaldo.isMaiorQue(this.limiteCredito)) {
                throw new RegraDeNegocioException(
                    "Limite de crédito excedido para o cliente '" + this.nome +
                    "'. Limite permitido: " + this.limiteCredito +
                    ", Saldo devedor total ficaria em: " + novoSaldo
                );
            }
        }

        this.saldoDevedor = this.saldoDevedor.somar(valor);
    }

    public void amortizarDebito(Dinheiro valorPago) {
        Objects.requireNonNull(valorPago, "Valor de amortização não pode ser nulo.");
        if (valorPago.isNegativo() || valorPago.isZero()) {
            throw new RegraDeNegocioException("Valor para amortização deve ser estritamente positivo.");
        }
        if (valorPago.isMaiorQue(this.saldoDevedor)) {
            throw new RegraDeNegocioException(
                "Valor para amortização (" + valorPago + ") é superior ao saldo devedor atual (" + this.saldoDevedor + ")."
            );
        }
        this.saldoDevedor = this.saldoDevedor.subtrair(valorPago);
    }

    public void estornarDebito(Dinheiro valor) {
        Objects.requireNonNull(valor, "Valor para estorno não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor para estorno deve ser estritamente positivo.");
        }
        if (valor.isMaiorQue(this.saldoDevedor)) {
            this.saldoDevedor = Dinheiro.zero();
        } else {
            this.saldoDevedor = this.saldoDevedor.subtrair(valor);
        }
    }

    public void atualizarLimiteCredito(Dinheiro novoLimite) {
        Objects.requireNonNull(novoLimite, "Novo limite de crédito não pode ser nulo.");
        if (novoLimite.isNegativo()) {
            throw new RegraDeNegocioException("Limite de crédito não pode ser negativo.");
        }
        this.limiteCredito = novoLimite;
    }

    public void atualizarContato(String nome, String telefone) {
        this.nome = validarNome(nome);
        this.telefone = telefone;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do cliente é obrigatório.");
        }
        return nome.trim();
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public Dinheiro getLimiteCredito() {
        return limiteCredito;
    }

    public Dinheiro getSaldoDevedor() {
        return saldoDevedor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
