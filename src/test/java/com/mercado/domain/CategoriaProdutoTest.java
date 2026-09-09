package com.mercado.domain;

import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Domínio: CategoriaProduto")
class CategoriaProdutoTest {

    private final TenantId tenantId = TenantId.de(UUID.randomUUID());

    @Test
    @DisplayName("Deve criar categoria com dados válidos e status ativo por padrão")
    void deveCriarCategoriaComSucesso() {
        CategoriaProduto categoria = CategoriaProduto.criar(tenantId, "Bebidas", "beer");

        assertNotNull(categoria.getId());
        assertEquals(tenantId, categoria.getTenantId());
        assertEquals("Bebidas", categoria.getNome());
        assertEquals("beer", categoria.getIcone());
        assertTrue(categoria.isAtivo());
        assertNotNull(categoria.getCriadoEm());
    }

    @Test
    @DisplayName("Deve falhar ao criar categoria com nome vazio ou nulo")
    void deveFalharAoCriarComNomeInvalido() {
        assertThrows(RegraDeNegocioException.class,
            () -> CategoriaProduto.criar(tenantId, "   ", "cart"));
        assertThrows(RegraDeNegocioException.class,
            () -> CategoriaProduto.criar(tenantId, null, "cart"));
    }

    @Test
    @DisplayName("Deve atualizar nome e ícone")
    void deveAtualizarNomeEIcone() {
        CategoriaProduto categoria = CategoriaProduto.criar(tenantId, "Higiene", "heart");
        categoria.atualizar("Higiene Pessoal", "sparkles");

        assertEquals("Higiene Pessoal", categoria.getNome());
        assertEquals("sparkles", categoria.getIcone());
    }

    @Test
    @DisplayName("Deve inativar e reativar categoria")
    void deveInativarEReativar() {
        CategoriaProduto categoria = CategoriaProduto.criar(tenantId, "Padaria", "bread");
        assertTrue(categoria.isAtivo());

        categoria.inativar();
        assertFalse(categoria.isAtivo());

        categoria.ativar();
        assertTrue(categoria.isAtivo());
    }
}
