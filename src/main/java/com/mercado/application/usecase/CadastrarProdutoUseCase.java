package com.mercado.application.usecase;

import com.mercado.application.dto.CriarProdutoInput;
import com.mercado.application.dto.ProdutoDTO;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Caso de Uso: Cadastrar Produto no catálogo.
 */
@ApplicationScoped
public class CadastrarProdutoUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public CadastrarProdutoUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    @Transactional
    public ProdutoDTO executar(CriarProdutoInput input) {
        Objects.requireNonNull(input, "Dados de criação do produto não podem ser nulos.");

        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para cadastrar produto.");
        }
        if (input.nome() == null || input.nome().isBlank()) {
            throw new RegraDeNegocioException("Nome do produto é obrigatório.");
        }
        if (input.precoVenda() == null || input.precoVenda().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Preço de venda do produto deve ser estritamente maior que zero.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Dinheiro preco = Dinheiro.de(input.precoVenda());

        Produto produto = Produto.criar(
            tenantId,
            input.nome(),
            preco,
            input.unidade(),
            input.estoqueInicial()
        );

        produtoRepository.salvar(produto);

        return ProdutoDTO.from(produto);
    }
}
