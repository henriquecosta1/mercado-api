package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produtos")
public class ProdutoJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "nome", nullable = false, length = 150)
    public String nome;

    @Column(name = "categoria", length = 50)
    public String categoria;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    public BigDecimal precoVenda;

    @Column(name = "preco_custo", precision = 12, scale = 2)
    public BigDecimal precoCusto;

    @Column(name = "unidade", nullable = false, length = 10)
    public String unidade;

    @Column(name = "estoque_atual", nullable = false, precision = 12, scale = 3)
    public BigDecimal estoqueAtual;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    public BigDecimal estoqueMinimo;

    @Column(name = "ativo", nullable = false)
    public boolean ativo;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public ProdutoJpaEntity() {
    }

    public static ProdutoJpaEntity fromDomain(Produto domain) {
        ProdutoJpaEntity entity = new ProdutoJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.nome = domain.getNome();
        entity.categoria = domain.getCategoria();
        entity.precoVenda = domain.getPrecoVenda().valor();
        entity.precoCusto = domain.getPrecoCusto() != null ? domain.getPrecoCusto().valor() : null;
        entity.unidade = domain.getUnidade();
        entity.estoqueAtual = domain.getEstoqueAtual();
        entity.estoqueMinimo = domain.getEstoqueMinimo();
        entity.ativo = domain.isAtivo();
        entity.criadoEm = Instant.now();
        return entity;
    }

    public void updateFromDomain(Produto domain) {
        this.nome = domain.getNome();
        this.categoria = domain.getCategoria();
        this.precoVenda = domain.getPrecoVenda().valor();
        this.precoCusto = domain.getPrecoCusto() != null ? domain.getPrecoCusto().valor() : null;
        this.unidade = domain.getUnidade();
        this.estoqueAtual = domain.getEstoqueAtual();
        this.estoqueMinimo = domain.getEstoqueMinimo();
        this.ativo = domain.isAtivo();
    }

    public Produto toDomain() {
        return new Produto(
            this.id,
            TenantId.de(this.tenantId),
            this.nome,
            this.categoria,
            Dinheiro.de(this.precoVenda),
            this.precoCusto != null ? Dinheiro.de(this.precoCusto) : null,
            this.unidade,
            this.estoqueAtual,
            this.estoqueMinimo != null ? this.estoqueMinimo : new BigDecimal("5.000"),
            this.ativo
        );
    }
}
