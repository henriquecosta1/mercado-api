package com.mercado.application.usecase;

import com.mercado.application.dto.ProdutoCarrinhoDTO;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de Uso: Busca rápida de produto por Código de Barras (EAN-13, GTIN)
 * ou Código Interno/Etiqueta de Balança (prefixo '2').
 * Projetado para checkout de frente de caixa (PDV) de alta velocidade (< 50ms).
 */
@ApplicationScoped
public class BuscarProdutoPorCodigoUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public BuscarProdutoPorCodigoUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    /**
     * Executa a busca do produto por código de barras ou código interno de balança.
     *
     * @param tenantId identificador único do tenant
     * @param codigo código de barras comercial ou código/etiqueta de balança
     * @return ProdutoCarrinhoDTO com dados essenciais do produto
     * @throws RecursoNaoEncontradoException se o produto não for encontrado
     */
    public ProdutoCarrinhoDTO executar(UUID tenantId, String codigo) {
        if (tenantId == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para busca de produto.");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new RecursoNaoEncontradoException("Código de produto não informado.");
        }

        String codigoLimpo = codigo.trim();

        // 1. Busca exata por código de barras comercial ou código interno/referência
        Optional<Produto> produtoOpt = produtoRepository.findByCodigoOuCodigoBarras(tenantId, codigoLimpo);

        // 2. Se não encontrar e o código for uma etiqueta de balança de 13 dígitos iniciada em '2'
        if (produtoOpt.isEmpty() && isEtiquetaBalanca(codigoLimpo)) {
            produtoOpt = resolverProdutoEtiquetaBalanca(tenantId, codigoLimpo);
        }

        Produto produto = produtoOpt.orElseThrow(() ->
            new RecursoNaoEncontradoException("Produto não encontrado para o código: " + codigoLimpo)
        );

        return ProdutoCarrinhoDTO.from(produto);
    }

    private boolean isEtiquetaBalanca(String codigo) {
        return codigo.length() == 13 && codigo.startsWith("2") && codigo.matches("^\\d{13}$");
    }

    /**
     * Extrai códigos internos de produtos a partir de etiquetas de balança comercial (padrão EAN-13 prefixo 2).
     * Suporta as principais configurações de balanças do mercado nacional (Toledo, Filizola, Elgin, Urano):
     * - Código interno de 5 dígitos (posições 1 a 6)
     * - Código interno de 4 dígitos (posições 1 a 5)
     * - Código interno de 6 dígitos (posições 1 a 7)
     * Realiza busca tanto com zeros à esquerda quanto normalizado sem zeros à esquerda.
     */
    private Optional<Produto> resolverProdutoEtiquetaBalanca(UUID tenantId, String codigo) {
        // Padrão mais frequente: 5 dígitos de código (posições 1 a 6, ex: 2 00015 00350 D -> "00015" / "15")
        String candidato5 = codigo.substring(1, 6);
        Optional<Produto> p5 = produtoRepository.findByCodigoOuCodigoBarras(tenantId, candidato5);
        if (p5.isPresent()) return p5;

        String c5SemZeros = removerZerosEsquerda(candidato5);
        if (!c5SemZeros.equals(candidato5)) {
            Optional<Produto> p5Trim = produtoRepository.findByCodigoOuCodigoBarras(tenantId, c5SemZeros);
            if (p5Trim.isPresent()) return p5Trim;
        }

        // Padrão 4 dígitos de código (posições 1 a 5, ex: 2 0015 000350 D -> "0015" / "15")
        String candidato4 = codigo.substring(1, 5);
        Optional<Produto> p4 = produtoRepository.findByCodigoOuCodigoBarras(tenantId, candidato4);
        if (p4.isPresent()) return p4;

        String c4SemZeros = removerZerosEsquerda(candidato4);
        if (!c4SemZeros.equals(candidato4)) {
            Optional<Produto> p4Trim = produtoRepository.findByCodigoOuCodigoBarras(tenantId, c4SemZeros);
            if (p4Trim.isPresent()) return p4Trim;
        }

        // Padrão 6 dígitos de código (posições 1 a 7, ex: 2 000015 0350 D -> "000015" / "15")
        String candidato6 = codigo.substring(1, 7);
        Optional<Produto> p6 = produtoRepository.findByCodigoOuCodigoBarras(tenantId, candidato6);
        if (p6.isPresent()) return p6;

        String c6SemZeros = removerZerosEsquerda(candidato6);
        if (!c6SemZeros.equals(candidato6)) {
            return produtoRepository.findByCodigoOuCodigoBarras(tenantId, c6SemZeros);
        }

        return Optional.empty();
    }

    private String removerZerosEsquerda(String str) {
        String semZeros = str.replaceFirst("^0+", "");
        return semZeros.isEmpty() ? "0" : semZeros;
    }
}
