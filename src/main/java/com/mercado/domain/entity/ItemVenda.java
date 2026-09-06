package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um item integrante de uma Venda.
 */
public class ItemVenda {

    private final UUID id;
    private final UUID produtoId;
    private final String descricao;
    private final BigDecimal quantidade;
    private final Dinheiro precoUnitario;
    private final Dinheiro subtotal;

    public ItemVenda(UUID id,
                     UUID produtoId,
                     String descricao,
                     BigDecimal quantidade,
                     Dinheiro precoUnitario,
                     Dinheiro subtotal) {
        this.id = Objects.requireNonNull(id, "Id do item não pode ser nulo.");
        this.produtoId = produtoId; // pode ser nulo se for item avulso sem produto cadastrado
        this.descricao = validarDescricao(descricao);
        this.quantidade = validarQuantidade(quantidade);
        this.precoUnitario = Objects.requireNonNull(precoUnitario, "Preço unitário não pode ser nulo.");
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal do item não pode ser nulo.");
    }

    public static ItemVenda criar(UUID produtoId,
                                  String descricao,
                                  BigDecimal quantidade,
                                  Dinheiro precoUnitario) {
        Objects.requireNonNull(precoUnitario, "Preço unitário não pode ser nulo.");
        if (precoUnitario.isNegativo() || precoUnitario.isZero()) {
            throw new RegraDeNegocioException("Preço unitário do item deve ser maior que zero.");
        }

        BigDecimal qtd = validarQuantidade(quantidade);
        BigDecimal subtotalCalculado = precoUnitario.valor()
            .multiply(qtd)
            .setScale(2, RoundingMode.HALF_EVEN);

        UUID id = UUID.randomUUID();
        return new ItemVenda(id, produtoId, descricao, qtd, precoUnitario, Dinheiro.de(subtotalCalculado));
    }

    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("Descrição do item da venda é obrigatória.");
        }
        return descricao.trim();
    }

    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        Objects.requireNonNull(quantidade, "Quantidade do item não pode ser nula.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade do item deve ser estritamente maior que zero.");
        }
        return quantidade.setScale(3, RoundingMode.HALF_EVEN);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public Dinheiro getPrecoUnitario() {
        return precoUnitario;
    }

    public Dinheiro getSubtotal() {
        return subtotal;
    }
}
