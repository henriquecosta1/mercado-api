package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando uma Categoria de Produto customizada por Loja/Tenant.
 * Totalmente desacoplada de frameworks.
 */
public class CategoriaProduto {

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private String icone;
    private boolean ativo;
    private final Instant criadoEm;

    public CategoriaProduto(UUID id,
                            TenantId tenantId,
                            String nome,
                            String icone,
                            boolean ativo,
                            Instant criadoEm) {
        this.id = Objects.requireNonNull(id, "Id da categoria não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.nome = validarNome(nome);
        this.icone = normalizarIcone(icone);
        this.ativo = ativo;
        this.criadoEm = criadoEm != null ? criadoEm : Instant.now();
    }

    public static CategoriaProduto criar(TenantId tenantId, String nome, String icone) {
        return new CategoriaProduto(
            UUID.randomUUID(),
            tenantId,
            nome,
            icone,
            true,
            Instant.now()
        );
    }

    public static CategoriaProduto criar(TenantId tenantId, String nome) {
        return criar(tenantId, nome, null);
    }

    public void atualizarNome(String novoNome) {
        this.nome = validarNome(novoNome);
    }

    public void atualizar(String novoNome, String novoIcone) {
        this.nome = validarNome(novoNome);
        this.icone = normalizarIcone(novoIcone);
    }

    public void inativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria é obrigatório.");
        }
        return nome.trim();
    }

    private static String normalizarIcone(String icone) {
        if (icone == null || icone.isBlank()) {
            return null;
        }
        return icone.trim().toLowerCase();
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

    public String getIcone() {
        return icone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
