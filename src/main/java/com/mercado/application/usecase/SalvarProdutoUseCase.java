package com.mercado.application.usecase;

import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.application.dto.SalvarProdutoInput;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Salvar Produto (Cadastro ou Edição Completa).
 * Se o ID for informado, atualiza o produto existente; caso contrário, cadastra um novo.
 */
@ApplicationScoped
public class SalvarProdutoUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public SalvarProdutoUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    @Transactional
    public ProdutoGerencialDTO executar(SalvarProdutoInput input) {
        Objects.requireNonNull(input, "Dados de entrada para salvar produto não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para salvar o produto.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Dinheiro precoVenda = input.precoVenda() != null ? Dinheiro.de(input.precoVenda()) : null;
        Dinheiro precoCusto = input.precoCusto() != null ? Dinheiro.de(input.precoCusto()) : null;
        Dinheiro precoPromocional = input.precoPromocional() != null ? Dinheiro.de(input.precoPromocional()) : null;

        Produto produto;
        if (input.id() == null) {
            // Criação de novo produto
            produto = Produto.criar(
                tenantId,
                input.nome(),
                input.categoria(),
                precoVenda,
                precoCusto,
                precoPromocional,
                input.unidade(),
                input.estoqueInicial(),
                input.estoqueMinimo(),
                input.codigoBarras(),
                input.codigoInterno(),
                input.permiteFracionado()
            );
        } else {
            // Atualização de produto existente
            produto = produtoRepository.buscarPorId(input.id(), tenantId)
                .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado com id: " + input.id()));

            produto.atualizarDados(
                input.nome(),
                input.categoria(),
                precoVenda,
                precoCusto,
                precoPromocional,
                input.unidade(),
                input.estoqueMinimo(),
                input.codigoBarras(),
                input.codigoInterno(),
                input.permiteFracionado() != null ? input.permiteFracionado() : produto.isPermiteFracionado()
            );
        }

        produtoRepository.salvar(produto);

        return ProdutoGerencialDTO.from(produto);
    }
}
