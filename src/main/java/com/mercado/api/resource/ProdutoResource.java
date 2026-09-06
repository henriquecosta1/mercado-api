package com.mercado.api.resource;

import com.mercado.api.dto.CriarProdutoRequest;
import com.mercado.application.dto.CriarProdutoInput;
import com.mercado.application.dto.ProdutoDTO;
import com.mercado.application.usecase.CadastrarProdutoUseCase;
import com.mercado.application.usecase.ListarProdutosUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para Catálogo de Produtos.
 * Mapeado sob /api/produtos, com execução em Virtual Threads do Java 21.
 */
@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final ListarProdutosUseCase listarProdutosUseCase;
    private final CadastrarProdutoUseCase cadastrarProdutoUseCase;

    @Inject
    public ProdutoResource(ListarProdutosUseCase listarProdutosUseCase,
                           CadastrarProdutoUseCase cadastrarProdutoUseCase) {
        this.listarProdutosUseCase = Objects.requireNonNull(listarProdutosUseCase, "ListarProdutosUseCase é obrigatório.");
        this.cadastrarProdutoUseCase = Objects.requireNonNull(cadastrarProdutoUseCase, "CadastrarProdutoUseCase é obrigatório.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarProdutos(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                  @QueryParam("busca") String busca) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        List<ProdutoDTO> produtos = listarProdutosUseCase.executar(tenantId, busca);
        return Response.ok(produtos).build();
    }

    @POST
    @RunOnVirtualThread
    public Response cadastrarProduto(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                    CriarProdutoRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser vazio.");
        }

        CriarProdutoInput input = new CriarProdutoInput(
            tenantId,
            request.nome(),
            request.precoVenda(),
            request.unidade(),
            request.estoqueInicial()
        );

        ProdutoDTO produtoCriado = cadastrarProdutoUseCase.executar(input);

        return Response.status(Response.Status.CREATED)
            .entity(produtoCriado)
            .build();
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
