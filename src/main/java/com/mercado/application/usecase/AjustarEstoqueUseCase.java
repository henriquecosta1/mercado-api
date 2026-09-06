package com.mercado.application.usecase;

import com.mercado.application.dto.AjustarEstoqueInput;
import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Ajuste Manual de Estoque Físico.
 * Atualiza o saldo físico diretamente com registro de motivo.
 */
@ApplicationScoped
public class AjustarEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public AjustarEstoqueUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    @Transactional
    public ProdutoGerencialDTO executar(AjustarEstoqueInput input) {
        Objects.requireNonNull(input, "Dados de entrada para ajuste de estoque não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para ajustar estoque.");
        }
        if (input.produtoId() == null) {
            throw new RegraDeNegocioException("Id do produto é obrigatório para ajustar estoque.");
        }
        if (input.novoEstoque() == null) {
            throw new RegraDeNegocioException("Novo valor de estoque é obrigatório.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());

        Produto produto = produtoRepository.buscarPorId(input.produtoId(), tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado com id: " + input.produtoId()));

        produto.ajustarEstoque(input.novoEstoque(), input.motivo());
        produtoRepository.salvar(produto);

        return ProdutoGerencialDTO.from(produto);
    }
}
