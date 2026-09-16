package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando um Produto no Catálogo e Estoque da loja.
 * Totalmente desacoplada de frameworks.
 */
public class Produto {

    public static final String CATEGORIA_PADRAO = "Geral";
    public static final BigDecimal ESTOQUE_MINIMO_PADRAO = new BigDecimal("5.000");

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private String categoria;
    private Dinheiro precoVenda;
    private Dinheiro precoCusto;
    private Dinheiro precoPromocional;
    private String unidade;
    private BigDecimal estoqueAtual;
    private BigDecimal estoqueMinimo;
    private boolean ativo;
    private String codigoBarras;
    private String codigoInterno;
    private boolean permiteFracionado;

    public Produto(UUID id,
                   TenantId tenantId,
                   String nome,
                   String categoria,
                   Dinheiro precoVenda,
                   Dinheiro precoCusto,
                   Dinheiro precoPromocional,
                   String unidade,
                   BigDecimal estoqueAtual,
                   BigDecimal estoqueMinimo,
                   boolean ativo,
                   String codigoBarras,
                   String codigoInterno,
                   boolean permiteFracionado) {
        this.id = Objects.requireNonNull(id, "Id do produto não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.nome = validarNome(nome);
        this.categoria = normalizarCategoria(categoria);
        this.precoVenda = validarPrecoVenda(precoVenda);
        this.precoCusto = validarPrecoCusto(precoCusto);
        this.precoPromocional = validarPrecoPromocional(precoPromocional, this.precoVenda);
        this.unidade = normalizarUnidade(unidade);
        this.estoqueAtual = estoqueAtual != null ? estoqueAtual.setScale(3, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(3);
        this.estoqueMinimo = normalizarEstoqueMinimo(estoqueMinimo);
        this.ativo = ativo;
        this.codigoBarras = normalizarCodigo(codigoBarras);
        this.codigoInterno = normalizarCodigo(codigoInterno);
        this.permiteFracionado = permiteFracionado;
    }

    public Produto(UUID id,
                   TenantId tenantId,
                   String nome,
                   String categoria,
                   Dinheiro precoVenda,
                   Dinheiro precoCusto,
                   String unidade,
                   BigDecimal estoqueAtual,
                   BigDecimal estoqueMinimo,
                   boolean ativo) {
        this(id, tenantId, nome, categoria, precoVenda, precoCusto, null, unidade, estoqueAtual, estoqueMinimo, ativo, null, null, isFracionadoPorUnidade(unidade));
    }

    public Produto(UUID id,
                   TenantId tenantId,
                   String nome,
                   Dinheiro precoVenda,
                   String unidade,
                   BigDecimal estoqueAtual,
                   boolean ativo) {
        this(id, tenantId, nome, CATEGORIA_PADRAO, precoVenda, null, unidade, estoqueAtual, ESTOQUE_MINIMO_PADRAO, ativo);
    }

    public static Produto criar(TenantId tenantId,
                                String nome,
                                String categoria,
                                Dinheiro precoVenda,
                                Dinheiro precoCusto,
                                Dinheiro precoPromocional,
                                String unidade,
                                BigDecimal estoqueInicial,
                                BigDecimal estoqueMinimo,
                                String codigoBarras,
                                String codigoInterno,
                                Boolean permiteFracionado) {
        UUID id = UUID.randomUUID();
        BigDecimal estoque = estoqueInicial != null ? estoqueInicial : BigDecimal.ZERO;
        boolean fracionado = permiteFracionado != null ? permiteFracionado : isFracionadoPorUnidade(unidade);
        return new Produto(
            id,
            tenantId,
            nome,
            categoria,
            precoVenda,
            precoCusto,
            precoPromocional,
            unidade,
            estoque,
            estoqueMinimo,
            true,
            codigoBarras,
            codigoInterno,
            fracionado
        );
    }

    public static Produto criar(TenantId tenantId,
                                String nome,
                                String categoria,
                                Dinheiro precoVenda,
                                Dinheiro precoCusto,
                                String unidade,
                                BigDecimal estoqueInicial,
                                BigDecimal estoqueMinimo) {
        return criar(tenantId, nome, categoria, precoVenda, precoCusto, null, unidade, estoqueInicial, estoqueMinimo, null, null, null);
    }

    public static Produto criar(TenantId tenantId,
                                String nome,
                                Dinheiro precoVenda,
                                String unidade,
                                BigDecimal estoqueInicial) {
        return criar(tenantId, nome, CATEGORIA_PADRAO, precoVenda, null, null, unidade, estoqueInicial, ESTOQUE_MINIMO_PADRAO, null, null, null);
    }

    public void atualizarDados(String nome,
                               String categoria,
                               Dinheiro precoVenda,
                               Dinheiro precoCusto,
                               Dinheiro precoPromocional,
                               String unidade,
                               BigDecimal estoqueMinimo,
                               String codigoBarras,
                               String codigoInterno,
                               boolean permiteFracionado) {
        this.nome = validarNome(nome);
        this.categoria = normalizarCategoria(categoria);
        this.precoVenda = validarPrecoVenda(precoVenda);
        this.precoCusto = validarPrecoCusto(precoCusto);
        this.precoPromocional = validarPrecoPromocional(precoPromocional, this.precoVenda);
        this.unidade = normalizarUnidade(unidade);
        this.estoqueMinimo = normalizarEstoqueMinimo(estoqueMinimo);
        this.codigoBarras = normalizarCodigo(codigoBarras);
        this.codigoInterno = normalizarCodigo(codigoInterno);
        this.permiteFracionado = permiteFracionado;
    }

    public void atualizarDados(String nome,
                               String categoria,
                               Dinheiro precoVenda,
                               Dinheiro precoCusto,
                               String unidade,
                               BigDecimal estoqueMinimo) {
        atualizarDados(nome, categoria, precoVenda, precoCusto, this.precoPromocional, unidade, estoqueMinimo, this.codigoBarras, this.codigoInterno, this.permiteFracionado);
    }

    public void atualizarDados(String nome, String unidade) {
        this.nome = validarNome(nome);
        this.unidade = normalizarUnidade(unidade);
    }

    public void definirCodigoBarras(String codigoBarras) {
        this.codigoBarras = normalizarCodigo(codigoBarras);
    }

    public void definirCodigoInterno(String codigoInterno) {
        this.codigoInterno = normalizarCodigo(codigoInterno);
    }

    public void definirPrecoPromocional(Dinheiro precoPromocional) {
        this.precoPromocional = validarPrecoPromocional(precoPromocional, this.precoVenda);
    }

    public void definirPermiteFracionado(boolean permiteFracionado) {
        this.permiteFracionado = permiteFracionado;
    }

    public void ajustarEstoque(BigDecimal novoEstoque, String motivo) {
        Objects.requireNonNull(novoEstoque, "Novo valor de estoque não pode ser nulo.");
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("Novo valor de estoque não pode ser negativo.");
        }
        this.estoqueAtual = novoEstoque.setScale(3, RoundingMode.HALF_EVEN);
    }

    public void baixarEstoque(BigDecimal quantidade) {
        Objects.requireNonNull(quantidade, "Quantidade para baixa de estoque não pode ser nula.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade para baixa de estoque deve ser estritamente maior que zero.");
        }
        this.estoqueAtual = this.estoqueAtual.subtract(quantidade).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void reporEstoque(BigDecimal quantidade) {
        Objects.requireNonNull(quantidade, "Quantidade para reposição de estoque não pode ser nula.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade para reposição de estoque deve ser estritamente maior que zero.");
        }
        this.estoqueAtual = this.estoqueAtual.add(quantidade).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void atualizarPreco(Dinheiro novoPreco) {
        this.precoVenda = validarPrecoVenda(novoPreco);
    }

    public void inativar() {
        this.ativo = false;
    }

    public void desativar() {
        inativar();
    }

    public void ativar() {
        this.ativo = true;
    }

    public boolean isEstoqueAbaixoDoMinimo() {
        return this.estoqueAtual.compareTo(this.estoqueMinimo) <= 0;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do produto é obrigatório.");
        }
        return nome.trim();
    }

    private static Dinheiro validarPrecoVenda(Dinheiro preco) {
        Objects.requireNonNull(preco, "Preço de venda não pode ser nulo.");
        if (preco.isNegativo() || preco.isZero()) {
            throw new RegraDeNegocioException("Preço de venda do produto deve ser maior que zero.");
        }
        return preco;
    }

    private static Dinheiro validarPrecoCusto(Dinheiro precoCusto) {
        if (precoCusto != null && precoCusto.isNegativo()) {
            throw new RegraDeNegocioException("Preço de custo não pode ser negativo.");
        }
        return precoCusto;
    }

    private static String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return CATEGORIA_PADRAO;
        }
        return categoria.trim();
    }

    private static String normalizarUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return "UN";
        }
        return unidade.trim().toUpperCase();
    }

    private static BigDecimal normalizarEstoqueMinimo(BigDecimal estoqueMinimo) {
        if (estoqueMinimo == null) {
            return ESTOQUE_MINIMO_PADRAO;
        }
        if (estoqueMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("Estoque mínimo não pode ser negativo.");
        }
        return estoqueMinimo.setScale(3, RoundingMode.HALF_EVEN);
    }

    private static Dinheiro validarPrecoPromocional(Dinheiro precoPromocional, Dinheiro precoVenda) {
        if (precoPromocional == null) {
            return null;
        }
        if (precoPromocional.isNegativo() || precoPromocional.isZero()) {
            throw new RegraDeNegocioException("Preço promocional deve ser estritamente maior que zero.");
        }
        if (precoVenda != null && precoPromocional.compareTo(precoVenda) >= 0) {
            throw new RegraDeNegocioException("Preço promocional deve ser menor que o preço de venda regular.");
        }
        return precoPromocional;
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return codigo.trim();
    }

    public static boolean isFracionadoPorUnidade(String unidade) {
        if (unidade == null) {
            return false;
        }
        String u = unidade.trim().toUpperCase();
        return u.equals("KG") || u.equals("G") || u.equals("LT") || u.equals("L") || u.equals("M") || u.equals("MT");
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public Dinheiro getPrecoVenda() {
        return precoVenda;
    }

    public Dinheiro getPrecoCusto() {
        return precoCusto;
    }

    public Dinheiro getPrecoPromocional() {
        return precoPromocional;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getUnidadeMedida() {
        return unidade;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public boolean isPermiteFracionado() {
        return permiteFracionado;
    }

    public boolean getPermiteFracionado() {
        return permiteFracionado;
    }
}
