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

    @Test
    @DisplayName("Deve verificar alerta de estoque abaixo do mínimo")
    void deveVerificarAlertaEstoqueBaixo() {
        Produto produto = Produto.criar(
            tenantId,
            "Sabonete",
            "Higiene",
            Dinheiro.de("3.00"),
            Dinheiro.de("1.50"),
            "UN",
            new BigDecimal("5.000"),
            new BigDecimal("10.000")
        );

        // Estoque atual (5.000) <= Estoque mínimo (10.000) -> deve dar alerta
        assertTrue(produto.isEstoqueAbaixoDoMinimo());

        produto.reporEstoque(new BigDecimal("10.000")); // Novo estoque 15.000
        assertFalse(produto.isEstoqueAbaixoDoMinimo());
    }

    @Test
    @DisplayName("Deve ajustar estoque físico manualmente e falhar com valor negativo")
    void deveAjustarEstoque() {
        Produto produto = Produto.criar(tenantId, "Café 500g", Dinheiro.de("15.00"), "PCT", new BigDecimal("10.000"));
        produto.ajustarEstoque(new BigDecimal("8.000"), "Contagem de inventário");

        assertEquals(new BigDecimal("8.000"), produto.getEstoqueAtual());

        assertThrows(RegraDeNegocioException.class,
            () -> produto.ajustarEstoque(new BigDecimal("-1.000"), "Inventário"));
    }

    @Test
    @DisplayName("Deve inativar e reativar produto")
    void deveInativarEAtivarProduto() {
        Produto produto = Produto.criar(tenantId, "Suco 1L", Dinheiro.de("8.00"), "UN", new BigDecimal("10.000"));
        assertTrue(produto.isAtivo());

        produto.inativar();
        assertFalse(produto.isAtivo());

        produto.ativar();
        assertTrue(produto.isAtivo());
    }

    @Test
    @DisplayName("Deve atualizar dados completos do produto")
    void deveAtualizarDadosCompletos() {
        Produto produto = Produto.criar(tenantId, "Leite", Dinheiro.de("4.00"), "UN", new BigDecimal("10.000"));

        produto.atualizarDados(
            "Leite Integral Tipo A",
            "Laticínios",
            Dinheiro.de("5.50"),
            Dinheiro.de("3.80"),
            "L",
            new BigDecimal("15.000")
        );

        assertEquals("Leite Integral Tipo A", produto.getNome());
        assertEquals("Laticínios", produto.getCategoria());
        assertEquals(new BigDecimal("5.50"), produto.getPrecoVenda().valor());
        assertEquals(new BigDecimal("3.80"), produto.getPrecoCusto().valor());
        assertEquals("L", produto.getUnidade());
        assertEquals(new BigDecimal("15.000"), produto.getEstoqueMinimo());
    }
}
