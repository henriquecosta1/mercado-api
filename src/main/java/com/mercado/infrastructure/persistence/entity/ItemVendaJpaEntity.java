package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.valueobject.Dinheiro;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "itens_venda")
public class ItemVendaJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "venda_id", nullable = false)
    public UUID vendaId;

    @Column(name = "produto_id")
    public UUID produtoId;

    @Column(name = "descricao", nullable = false, length = 150)
    public String descricao;

    @Column(name = "quantidade", nullable = false, precision = 12, scale = 3)
    public BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    public BigDecimal precoUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    public BigDecimal subtotal;

    public ItemVendaJpaEntity() {
    }

    public static ItemVendaJpaEntity fromDomain(ItemVenda domain, UUID vendaId) {
        ItemVendaJpaEntity entity = new ItemVendaJpaEntity();
        entity.id = domain.getId();
        entity.vendaId = vendaId;
        entity.produtoId = domain.getProdutoId();
        entity.descricao = domain.getDescricao();
        entity.quantidade = domain.getQuantidade();
        entity.precoUnitario = domain.getPrecoUnitario().valor();
        entity.subtotal = domain.getSubtotal().valor();
        return entity;
    }

    public ItemVenda toDomain() {
        return new ItemVenda(
            this.id,
            this.produtoId,
            this.descricao,
            this.quantidade,
            Dinheiro.de(this.precoUnitario),
            Dinheiro.de(this.subtotal)
        );
    }
}
