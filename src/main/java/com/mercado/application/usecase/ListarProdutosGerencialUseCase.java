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
}
