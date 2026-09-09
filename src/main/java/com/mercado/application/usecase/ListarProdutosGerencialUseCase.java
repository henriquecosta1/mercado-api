package com.mercado.application.usecase;

import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Listar Produtos na visão gerencial com filtros dinâmicos.
 * Permite filtrar por busca textual de nome, categoria e flag de estoque baixo.
 */
@ApplicationScoped
public class ListarProdutosGerencialUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public ListarProdutosGerencialUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    public List<ProdutoGerencialDTO> executar(UUID tenantIdUuid, String busca, String categoria, Boolean apenasEstoqueBaixo) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para listar produtos.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);
        List<Produto> produtos = produtoRepository.listarGerencial(tenantId, busca, categoria, apenasEstoqueBaixo);

        return produtos.stream()
            .map(ProdutoGerencialDTO::from)
            .toList();
    }

    public com.mercado.application.dto.PageDTO<ProdutoGerencialDTO> executarPaginado(UUID tenantIdUuid, String busca, String categoria, Boolean apenasEstoqueBaixo, int page, int size) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para listar produtos.");
        }

        int paginaEfetiva = Math.max(0, page);
        int tamanhoEfetivo = Math.min(Math.max(1, size), 100);

        TenantId tenantId = TenantId.de(tenantIdUuid);
        com.mercado.domain.repository.PageResult<Produto> pageResult = produtoRepository.listarGerencialPaginado(
            tenantId, busca, categoria, apenasEstoqueBaixo, paginaEfetiva, tamanhoEfetivo
        );

        List<ProdutoGerencialDTO> dtos = pageResult.content().stream()
            .map(ProdutoGerencialDTO::from)
            .toList();

        return com.mercado.application.dto.PageDTO.of(dtos, pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
