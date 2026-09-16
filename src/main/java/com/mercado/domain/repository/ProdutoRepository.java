package com.mercado.domain.repository;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.valueobject.TenantId;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para a entidade Produto (DIP - Dependency Inversion Principle).
 */
public interface ProdutoRepository {

    void salvar(Produto produto);

    Optional<Produto> buscarPorId(UUID id, TenantId tenantId);

    List<Produto> buscarPorNome(String termo, TenantId tenantId);

    List<Produto> listarAtivos(TenantId tenantId);

    /**
     * Busca rápida de produto ativo por código de barras comercial ou código interno/referência de balança.
     * Filtro estrito por tenantId garantindo isolamento multi-tenant seguro e resposta instantânea para o PDV.
     *
     * @param tenantId identificador único do estabelecimento (Tenant)
     * @param codigo código de barras (EAN/GTIN) ou código interno de balança
     * @return Optional contendo o produto ativo se encontrado
     */
    default Optional<Produto> findByCodigoOuCodigoBarras(UUID tenantId, String codigo) {
        return Optional.empty();
    }

    default Optional<Produto> findByCodigoOuCodigoBarras(TenantId tenantId, String codigo) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return findByCodigoOuCodigoBarras(tenantId.valor(), codigo);
    }

    default List<Produto> listarGerencial(TenantId tenantId, String busca, String categoria, Boolean apenasEstoqueBaixo) {
        return Collections.emptyList();
    }

    default PageResult<Produto> listarGerencialPaginado(TenantId tenantId, String busca, String categoria, Boolean apenasEstoqueBaixo, int page, int size) {
        List<Produto> todos = listarGerencial(tenantId, busca, categoria, apenasEstoqueBaixo);
        int fromIndex = Math.min(page * size, todos.size());
        int toIndex = Math.min(fromIndex + size, todos.size());
        List<Produto> subList = fromIndex <= toIndex ? todos.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(subList, page, size, todos.size());
    }

    default void excluirOuInativar(UUID id, TenantId tenantId) {
        buscarPorId(id, tenantId).ifPresent(produto -> {
            produto.inativar();
            salvar(produto);
        });
    }
}
