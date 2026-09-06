package com.mercado.domain;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Domínio: Produto (Entidade Rica)")
class ProdutoTest {

    private final TenantId tenantId = TenantId.de(UUID.randomUUID());

    @Test
    @DisplayName("Deve criar produto com dados válidos")
    void deveCriarProdutoComSucesso() {
        Produto produto = Produto.criar(tenantId, "Arroz 5kg", Dinheiro.de("28.90"), "PCT", new BigDecimal("50.000"));

        assertNotNull(produto.getId());
        assertEquals("Arroz 5kg", produto.getNome());
        assertEquals(new BigDecimal("28.90"), produto.getPrecoVenda().valor());
        assertEquals("PCT", produto.getUnidade());
        assertEquals(new BigDecimal("50.000"), produto.getEstoqueAtual());
        assertTrue(produto.isAtivo());
    }

    @Test
    @DisplayName("Deve baixar estoque corretamente")
    void deveBaixarEstoque() {
        Produto produto = Produto.criar(tenantId, "Feijão 1kg", Dinheiro.de("7.50"), "PCT", new BigDecimal("20.000"));
        produto.baixarEstoque(new BigDecimal("3.000"));

        assertEquals(new BigDecimal("17.000"), produto.getEstoqueAtual());
    }

    @Test
    @DisplayName("Deve repor estoque corretamente")
    void deveReporEstoque() {
        Produto produto = Produto.criar(tenantId, "Óleo de Soja", Dinheiro.de("6.00"), "UN", new BigDecimal("10.000"));
        produto.reporEstoque(new BigDecimal("15.000"));

        assertEquals(new BigDecimal("25.000"), produto.getEstoqueAtual());
    }

    @Test
    @DisplayName("Deve falhar ao baixar estoque com quantidade inválida")
    void deveFalharBaixarEstoqueInvalido() {
        Produto produto = Produto.criar(tenantId, "Sal 1kg", Dinheiro.de("2.00"), "UN", new BigDecimal("10.000"));

        assertThrows(RegraDeNegocioException.class, () -> produto.baixarEstoque(BigDecimal.ZERO));
        assertThrows(RegraDeNegocioException.class, () -> produto.baixarEstoque(new BigDecimal("-1.000")));
    }

    @Test
    @DisplayName("Deve atualizar preço de venda")
    void deveAtualizarPreco() {
        Produto produto = Produto.criar(tenantId, "Açúcar 1kg", Dinheiro.de("4.00"), "PCT", new BigDecimal("30.000"));
        produto.atualizarPreco(Dinheiro.de("4.50"));

        assertEquals(new BigDecimal("4.50"), produto.getPrecoVenda().valor());
    }
}
