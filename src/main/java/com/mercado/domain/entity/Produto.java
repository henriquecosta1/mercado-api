package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando um Produto no Catálogo da loja.
 */
public class Produto {

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private Dinheiro precoVenda;
    private String unidade;
    private BigDecimal estoqueAtual;
    private boolean ativo;

    public Produto(UUID id,
                   TenantId tenantId,
                   String nome,
                   Dinheiro precoVenda,
                   String unidade,
                   BigDecimal estoqueAtual,
                   boolean ativo) {
        this.id = Objects.requireNonNull(id, "Id do produto não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.nome = validarNome(nome);
        this.precoVenda = validarPrecoVenda(precoVenda);
        this.unidade = normalizarUnidade(unidade);
        this.estoqueAtual = estoqueAtual != null ? estoqueAtual.setScale(3, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(3);
        this.ativo = ativo;
    }

    public static Produto criar(TenantId tenantId,
                                String nome,
                                Dinheiro precoVenda,
                                String unidade,
                                BigDecimal estoqueInicial) {
        UUID id = UUID.randomUUID();
        BigDecimal estoque = estoqueInicial != null ? estoqueInicial : BigDecimal.ZERO;
        return new Produto(id, tenantId, nome, precoVenda, unidade, estoque, true);
    }

    public void baixarEstoque(BigDecimal quantidade) {
        Objects.requireNonNull(quantidade, "Quantidade para baixa de estoque não pode ser nula.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade para baixa de estoque deve ser estritamente maior que zero.");
        }
        this.estoqueAtual = this.estoqueAtual.subtract(quantidade).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void reporEstoque(BigDecimal quantidade) {
        Objects.requireNonNull(quantidade, "Quantidade para reposição de estoque não pode ser nula.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade para reposição de estoque deve ser estritamente maior que zero.");
        }
        this.estoqueAtual = this.estoqueAtual.add(quantidade).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void atualizarPreco(Dinheiro novoPreco) {
        this.precoVenda = validarPrecoVenda(novoPreco);
    }

    public void atualizarDados(String nome, String unidade) {
        this.nome = validarNome(nome);
        this.unidade = normalizarUnidade(unidade);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do produto é obrigatório.");
        }
        return nome.trim();
    }

    private static Dinheiro validarPrecoVenda(Dinheiro preco) {
        Objects.requireNonNull(preco, "Preço de venda não pode ser nulo.");
        if (preco.isNegativo() || preco.isZero()) {
            throw new RegraDeNegocioException("Preço de venda do produto deve ser maior que zero.");
        }
        return preco;
    }

    private static String normalizarUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return "UN";
        }
        return unidade.trim().toUpperCase();
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

    public Dinheiro getPrecoVenda() {
        return precoVenda;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
