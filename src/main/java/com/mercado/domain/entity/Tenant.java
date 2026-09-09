package com.mercado.domain.entity;

import com.mercado.domain.exception.PinInvalidoException;
import com.mercado.domain.valueobject.PinGerente;
import com.mercado.domain.valueobject.TenantId;

import java.util.Objects;

/**
 * Entidade rica representando o Inquilino / Loja (Tenant).
 */
public class Tenant {

    private final TenantId id;
    private String nome;
    private PinGerente pinGerente;

    public Tenant(TenantId id, String nome, PinGerente pinGerente) {
        this.id = Objects.requireNonNull(id, "TenantId não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "Nome do tenant não pode ser nulo.");
        this.pinGerente = Objects.requireNonNull(pinGerente, "PinGerente não pode ser nulo.");
    }

    public static Tenant criar(String nome, String pinGerente) {
        TenantId tenantId = TenantId.de(java.util.UUID.randomUUID());
        String pinValido = (pinGerente != null && !pinGerente.isBlank()) ? pinGerente : "1234";
        return new Tenant(tenantId, nome.trim(), PinGerente.criar(pinValido));
    }

    public static Tenant criar(String nome) {
        return criar(nome, "1234");
    }

    public void validarPin(String pinCandidato) {
        if (pinCandidato == null || !this.pinGerente.verificar(pinCandidato)) {
            throw new PinInvalidoException("PIN de gerente inválido ou não informado.");
        }
    }

    public void alterarPin(String pinAtual, String novoPin) {
        if (pinAtual == null || !this.pinGerente.verificar(pinAtual)) {
            throw new PinInvalidoException("PIN atual incorreto.");
        }
        this.pinGerente = PinGerente.criar(novoPin);
    }

    public TenantId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public PinGerente getPinGerente() {
        return pinGerente;
    }
}
