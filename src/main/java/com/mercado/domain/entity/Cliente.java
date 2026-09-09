package com.mercado.domain.entity;

import com.mercado.domain.exception.ClienteBloqueadoException;
import com.mercado.domain.exception.LimiteCreditoExcedidoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando o Cliente da loja (com suporte a analise de risco e conta fiado).
 * Totalmente desacoplada de frameworks.
 */
public class Cliente {

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private String apelido;
    private String telefone;
    private String cpf;
    private String endereco;
    private String pontoReferencia;
    private Dinheiro limiteCredito;
    private Dinheiro saldoDevedor;
    private Integer diaVencimento;
    private StatusCliente status;
    private String motivoBloqueio;
    private String observacoes;
    private final LocalDateTime criadoEm;

    public Cliente(UUID id,
                   TenantId tenantId,
                   String nome,
                   String apelido,
                   String telefone,
                   String cpf,
                   String endereco,
                   String pontoReferencia,
                   Dinheiro limiteCredito,
                   Dinheiro saldoDevedor,
                   Integer diaVencimento,
                   StatusCliente status,
                   String motivoBloqueio,
                   String observacoes,
                   LocalDateTime criadoEm) {
        this.id = Objects.requireNonNull(id, "Id do cliente nao pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId nao pode ser nulo.");
        this.nome = validarNome(nome);
        this.apelido = apelido;
        this.telefone = telefone;
        this.cpf = cpf;
        this.endereco = endereco;
        this.pontoReferencia = pontoReferencia;
        this.limiteCredito = Objects.requireNonNull(limiteCredito, "Limite de credito nao pode ser nulo.");
        this.saldoDevedor = Objects.requireNonNull(saldoDevedor, "Saldo devedor nao pode ser nulo.");
        this.diaVencimento = diaVencimento != null ? diaVencimento : 10;
        this.status = status != null ? status : StatusCliente.ATIVO;
        this.motivoBloqueio = motivoBloqueio;
        this.observacoes = observacoes;
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criacao nao pode ser nula.");
    }

    public static Cliente criar(TenantId tenantId,
                                String nome,
                                String apelido,
                                String telefone,
                                String cpf,
                                String endereco,
                                String pontoReferencia,
                                Dinheiro limiteCredito,
                                Integer diaVencimento,
                                String observacoes) {
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        Dinheiro limite = limiteCredito != null ? limiteCredito : Dinheiro.zero();
        Integer vencimento = (diaVencimento != null && diaVencimento >= 1 && diaVencimento <= 31) ? diaVencimento : 10;
        return new Cliente(
            id,
            tenantId,
            nome,
            apelido,
            telefone,
            cpf,
            endereco,
            pontoReferencia,
            limite,
            Dinheiro.zero(),
            vencimento,
            StatusCliente.ATIVO,
            null,
            observacoes,
            agora
        );
    }

    public static Cliente criar(TenantId tenantId, String nome, String telefone, Dinheiro limiteCredito) {
        return criar(tenantId, nome, null, telefone, null, null, null, limiteCredito, 10, null);
    }

    public void atualizarDados(String nome,
                               String apelido,
                               String telefone,
                               String cpf,
                               String endereco,
                               String pontoReferencia,
                               Dinheiro limiteCredito,
                               Integer diaVencimento,
                               String observacoes) {
        this.nome = validarNome(nome);
        this.apelido = apelido != null ? apelido.trim() : null;
        this.telefone = telefone != null ? telefone.trim() : null;
        this.cpf = cpf != null ? cpf.trim() : null;
        this.endereco = endereco != null ? endereco.trim() : null;
        this.pontoReferencia = pontoReferencia != null ? pontoReferencia.trim() : null;
        if (limiteCredito != null) {
            atualizarLimiteCredito(limiteCredito);
        }
        if (diaVencimento != null) {
            if (diaVencimento < 1 || diaVencimento > 31) {
                throw new RegraDeNegocioException("Dia de vencimento deve estar entre 1 e 31.");
            }
            this.diaVencimento = diaVencimento;
        }
        this.observacoes = observacoes != null ? observacoes.trim() : null;
    }

    public void bloquear(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do bloqueio e obrigatorio.");
        }
        this.status = StatusCliente.BLOQUEADO;
        this.motivoBloqueio = motivo.trim();
    }

    public void desbloquear() {
        this.status = StatusCliente.ATIVO;
        this.motivoBloqueio = null;
    }

    public void inativar() {
        this.status = StatusCliente.INATIVO;
    }

    public boolean podeSerExcluido() {
        return this.saldoDevedor == null || this.saldoDevedor.isZero();
    }

    public void validarVendaFiado(Dinheiro valorVenda) {
        Objects.requireNonNull(valorVenda, "Valor da venda nao pode ser nulo.");
        if (valorVenda.isNegativo() || valorVenda.isZero()) {
            throw new RegraDeNegocioException("Valor da venda fiado deve ser estritamente positivo.");
        }

        if (this.status != StatusCliente.ATIVO) {
            String motivo = (this.motivoBloqueio != null && !this.motivoBloqueio.isBlank())
                ? this.motivoBloqueio
                : "Cliente com status " + this.status;
            throw new ClienteBloqueadoException("Cliente bloqueado para novas compras: " + motivo);
        }

        if (this.limiteCredito.isMaiorQue(Dinheiro.zero())) {
            Dinheiro novoSaldo = this.saldoDevedor.somar(valorVenda);
            if (novoSaldo.isMaiorQue(this.limiteCredito)) {
                throw new LimiteCreditoExcedidoException(
                    "Compra excede o limite de credito do cliente '" + this.nome +
                    "'. Limite permitido: " + this.limiteCredito +
                    ", Saldo devedor total ficaria em: " + novoSaldo
                );
            }
        }
    }

    public void registrarDebito(Dinheiro valor) {
        validarVendaFiado(valor);
        this.saldoDevedor = this.saldoDevedor.somar(valor);
    }

    public void amortizarDebito(Dinheiro valorPago) {
        Objects.requireNonNull(valorPago, "Valor de amortizacao nao pode ser nulo.");
        if (valorPago.isNegativo() || valorPago.isZero()) {
            throw new RegraDeNegocioException("Valor para amortizacao deve ser estritamente positivo.");
        }
        if (valorPago.isMaiorQue(this.saldoDevedor)) {
            throw new RegraDeNegocioException(
                "Valor para amortizacao (" + valorPago + ") e superior ao saldo devedor atual (" + this.saldoDevedor + ")."
            );
        }
        this.saldoDevedor = this.saldoDevedor.subtrair(valorPago);
    }

    public void estornarDebito(Dinheiro valor) {
        Objects.requireNonNull(valor, "Valor para estorno nao pode ser nulo.");
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
        Objects.requireNonNull(novoLimite, "Novo limite de credito nao pode ser nulo.");
        if (novoLimite.isNegativo()) {
            throw new RegraDeNegocioException("Limite de credito nao pode ser negativo.");
        }
        this.limiteCredito = novoLimite;
    }

    public void atualizarContato(String nome, String telefone) {
        this.nome = validarNome(nome);
        this.telefone = telefone;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do cliente e obrigatorio.");
        }
        return nome.trim();
    }

    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getNome() { return nome; }
    public String getApelido() { return apelido; }
    public String getTelefone() { return telefone; }
    public String getCpf() { return cpf; }
    public String getEndereco() { return endereco; }
    public String getPontoReferencia() { return pontoReferencia; }
    public Dinheiro getLimiteCredito() { return limiteCredito; }
    public Dinheiro getSaldoDevedor() { return saldoDevedor; }
    public Integer getDiaVencimento() { return diaVencimento; }
    public StatusCliente getStatus() { return status; }
    public String getMotivoBloqueio() { return motivoBloqueio; }
    public String getObservacoes() { return observacoes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}