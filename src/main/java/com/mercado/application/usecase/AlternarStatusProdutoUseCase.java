package com.mercado.application.usecase;

import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Alternar Status do Produto (Ativar / Inativar no Catálogo).
 */
@ApplicationScoped
public class AlternarStatusProdutoUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public AlternarStatusProdutoUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    @Transactional
    public ProdutoGerencialDTO executar(UUID tenantIdUuid, UUID produtoId) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para alternar status do produto.");
        }
        if (produtoId == null) {
            throw new RegraDeNegocioException("Id do produto é obrigatório.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        Produto produto = produtoRepository.buscarPorId(produtoId, tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado com id: " + produtoId));

        if (produto.isAtivo()) {
            produto.inativar();
        } else {
            produto.ativar();
        }

        produtoRepository.salvar(produto);

        return ProdutoGerencialDTO.from(produto);
    }
}
