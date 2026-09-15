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
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
 * Recurso REST para Catalogo e Gestao de Estoque de Produtos.
 * Mapeado sob /api/produtos, com execucao em Virtual Threads do Java 21.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    private final ListarProdutosGerencialUseCase listarProdutosGerencialUseCase;
    private final SalvarProdutoUseCase salvarProdutoUseCase;
    private final AjustarEstoqueUseCase ajustarEstoqueUseCase;
    private final AlternarStatusProdutoUseCase alternarStatusProdutoUseCase;
    private final ProdutoRepository produtoRepository;
    private final TenantSecurityContext securityContext;

    @Inject
    public ProdutoResource(ListarProdutosGerencialUseCase listarProdutosGerencialUseCase,
                           SalvarProdutoUseCase salvarProdutoUseCase,
                           AjustarEstoqueUseCase ajustarEstoqueUseCase,
                           AlternarStatusProdutoUseCase alternarStatusProdutoUseCase,
                           ProdutoRepository produtoRepository,
                           TenantSecurityContext securityContext) {
        this.listarProdutosGerencialUseCase = Objects.requireNonNull(listarProdutosGerencialUseCase, "ListarProdutosGerencialUseCase e obrigatorio.");
        this.salvarProdutoUseCase = Objects.requireNonNull(salvarProdutoUseCase, "SalvarProdutoUseCase e obrigatorio.");
        this.ajustarEstoqueUseCase = Objects.requireNonNull(ajustarEstoqueUseCase, "AjustarEstoqueUseCase e obrigatorio.");
        this.alternarStatusProdutoUseCase = Objects.requireNonNull(alternarStatusProdutoUseCase, "AlternarStatusProdutoUseCase e obrigatorio.");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarProdutos(@QueryParam("busca") String busca,
                                   @QueryParam("categoria") String categoria,
                                   @QueryParam("estoqueBaixo") Boolean estoqueBaixo,
                                   @QueryParam("page") Integer page,
                                   @QueryParam("size") Integer size) {
        UUID tenantId = securityContext.getTenantId().valor();

        if (page != null || size != null) {
            int pagina = page != null ? page : 0;
            int tamanho = size != null ? size : 10;
            com.mercado.application.dto.PageDTO<ProdutoGerencialDTO> paginado = listarProdutosGerencialUseCase.executarPaginado(
                tenantId, busca, categoria, estoqueBaixo, pagina, tamanho
            );
            return Response.ok(paginado).build();
        }

        List<ProdutoGerencialDTO> produtos = listarProdutosGerencialUseCase.executar(tenantId, busca, categoria, estoqueBaixo);
        return Response.ok(produtos).build();
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response buscarPorId(@PathParam("id") UUID id) {
        UUID tenantId = securityContext.getTenantId().valor();
        Produto produto = produtoRepository.buscarPorId(id, TenantId.de(tenantId))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado com id: " + id));

        return Response.ok(ProdutoGerencialDTO.from(produto)).build();
    }

    @POST
    @RolesAllowed({"GERENTE", "ADMIN"})
    @RunOnVirtualThread
    public Response cadastrarProduto(SalvarProdutoRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
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
    @RolesAllowed({"GERENTE", "ADMIN"})
    @Path("/{id}")
    @RunOnVirtualThread
    public Response atualizarProduto(@PathParam("id") UUID id,
                                     SalvarProdutoRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
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
    @RolesAllowed({"GERENTE", "ADMIN"})
    @Path("/{id}/estoque")
    @RunOnVirtualThread
    public Response ajustarEstoque(@PathParam("id") UUID id,
                                   AjustarEstoqueRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
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
    @RolesAllowed({"GERENTE", "ADMIN"})
    @Path("/{id}/status")
    @RunOnVirtualThread
    public Response alternarStatus(@PathParam("id") UUID id) {
        UUID tenantId = securityContext.getTenantId().valor();
        ProdutoGerencialDTO produtoAtualizado = alternarStatusProdutoUseCase.executar(tenantId, id);
        return Response.ok(produtoAtualizado).build();
    }
}