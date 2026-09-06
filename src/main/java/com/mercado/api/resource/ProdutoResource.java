package com.mercado.api.resource;

import com.mercado.api.dto.AjustarEstoqueRequest;
import com.mercado.api.dto.SalvarProdutoRequest;
import com.mercado.application.dto.AjustarEstoqueInput;
import com.mercado.application.dto.ProdutoGerencialDTO;
import com.mercado.application.dto.SalvarProdutoInput;
import com.mercado.application.usecase.AjustarEstoqueUseCase;
import com.mercado.application.usecase.AlternarStatusProdutoUseCase;
import com.mercado.application.usecase.ListarProdutosGerencialUseCase;
import com.mercado.application.usecase.SalvarProdutoUseCase;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para Catálogo e Gestão de Estoque de Produtos.
 * Mapeado sob /api/produtos, com execução em Virtual Threads do Java 21.
 */
@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final ListarProdutosGerencialUseCase listarProdutosGerencialUseCase;
    private final SalvarProdutoUseCase salvarProdutoUseCase;
    private final AjustarEstoqueUseCase ajustarEstoqueUseCase;
    private final AlternarStatusProdutoUseCase alternarStatusProdutoUseCase;
    private final ProdutoRepository produtoRepository;

    @Inject
    public ProdutoResource(ListarProdutosGerencialUseCase listarProdutosGerencialUseCase,
                           SalvarProdutoUseCase salvarProdutoUseCase,
                           AjustarEstoqueUseCase ajustarEstoqueUseCase,
                           AlternarStatusProdutoUseCase alternarStatusProdutoUseCase,
                           ProdutoRepository produtoRepository) {
        this.listarProdutosGerencialUseCase = Objects.requireNonNull(listarProdutosGerencialUseCase, "ListarProdutosGerencialUseCase é obrigatório.");
        this.salvarProdutoUseCase = Objects.requireNonNull(salvarProdutoUseCase, "SalvarProdutoUseCase é obrigatório.");
        this.ajustarEstoqueUseCase = Objects.requireNonNull(ajustarEstoqueUseCase, "AjustarEstoqueUseCase é obrigatório.");
        this.alternarStatusProdutoUseCase = Objects.requireNonNull(alternarStatusProdutoUseCase, "AlternarStatusProdutoUseCase é obrigatório.");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarProdutos(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                   @QueryParam("busca") String busca,
                                   @QueryParam("categoria") String categoria,
                                   @QueryParam("estoqueBaixo") Boolean estoqueBaixo) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        List<ProdutoGerencialDTO> produtos = listarProdutosGerencialUseCase.executar(tenantId, busca, categoria, estoqueBaixo);
        return Response.ok(produtos).build();
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response buscarPorId(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                @PathParam("id") UUID id) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        Produto produto = produtoRepository.buscarPorId(id, TenantId.de(tenantId))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com id: " + id));

        return Response.ok(ProdutoGerencialDTO.from(produto)).build();
    }

    @POST
    @RunOnVirtualThread
    public Response cadastrarProduto(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                     SalvarProdutoRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser vazio.");
        }

        SalvarProdutoInput input = new SalvarProdutoInput(
            tenantId,
            null,
            request.nome(),
            request.categoria(),
            request.precoVenda(),
            request.precoCusto(),
            request.unidade(),
            request.estoqueInicial(),
            request.estoqueMinimo()
        );

        ProdutoGerencialDTO produtoCriado = salvarProdutoUseCase.executar(input);

        return Response.status(Response.Status.CREATED)
            .entity(produtoCriado)
            .build();
    }

    @PUT
    @Path("/{id}")
    @RunOnVirtualThread
    public Response atualizarProduto(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                     @PathParam("id") UUID id,
                                     SalvarProdutoRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser vazio.");
        }

        SalvarProdutoInput input = new SalvarProdutoInput(
            tenantId,
            id,
            request.nome(),
            request.categoria(),
            request.precoVenda(),
            request.precoCusto(),
            request.unidade(),
            request.estoqueInicial(),
            request.estoqueMinimo()
        );

        ProdutoGerencialDTO produtoAtualizado = salvarProdutoUseCase.executar(input);

        return Response.ok(produtoAtualizado).build();
    }

    @PATCH
    @Path("/{id}/estoque")
    @RunOnVirtualThread
    public Response ajustarEstoque(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                   @PathParam("id") UUID id,
                                   AjustarEstoqueRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser vazio.");
        }

        AjustarEstoqueInput input = new AjustarEstoqueInput(
            tenantId,
            id,
            request.novoEstoque(),
            request.motivo()
        );

        ProdutoGerencialDTO produtoAtualizado = ajustarEstoqueUseCase.executar(input);

        return Response.ok(produtoAtualizado).build();
    }

    @PATCH
    @Path("/{id}/status")
    @RunOnVirtualThread
    public Response alternarStatus(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                   @PathParam("id") UUID id) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        ProdutoGerencialDTO produtoAtualizado = alternarStatusProdutoUseCase.executar(tenantId, id);
        return Response.ok(produtoAtualizado).build();
    }

    private UUID extrairTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            throw new IllegalArgumentException("O cabeçalho obrigatório '" + HEADER_TENANT_ID + "' não foi informado.");
        }
        try {
            return UUID.fromString(tenantIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Identificador de Tenant ('" + HEADER_TENANT_ID + "') com formato UUID inválido: " + tenantIdHeader);
        }
    }
}
