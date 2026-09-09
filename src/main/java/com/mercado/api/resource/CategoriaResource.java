package com.mercado.api.resource;

import com.mercado.api.dto.SalvarCategoriaRequest;
import com.mercado.application.dto.AtualizarCategoriaInput;
import com.mercado.application.dto.CategoriaDTO;
import com.mercado.application.dto.CriarCategoriaInput;
import com.mercado.application.usecase.GerenciarCategoriasUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para Gestão de Categorias de Produtos Customizadas por Loja.
 * Executa em Virtual Threads do Java 21 via @RunOnVirtualThread.
 * Requer autenticação JWT válida (@Authenticated).
 */
@Authenticated
@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    private final GerenciarCategoriasUseCase gerenciarCategoriasUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public CategoriaResource(GerenciarCategoriasUseCase gerenciarCategoriasUseCase,
                             TenantSecurityContext securityContext) {
        this.gerenciarCategoriasUseCase = Objects.requireNonNull(gerenciarCategoriasUseCase, "GerenciarCategoriasUseCase é obrigatório.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext é obrigatório.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarCategorias() {
        UUID tenantId = securityContext.getTenantId().valor();
        List<CategoriaDTO> categorias = gerenciarCategoriasUseCase.listar(tenantId);
        return Response.ok(categorias).build();
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response buscarPorId(@PathParam("id") UUID id) {
        UUID tenantId = securityContext.getTenantId().valor();
        CategoriaDTO categoria = gerenciarCategoriasUseCase.buscarPorId(tenantId, id);
        return Response.ok(categoria).build();
    }

    @POST
    @RunOnVirtualThread
    public Response criarCategoria(SalvarCategoriaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        CriarCategoriaInput input = new CriarCategoriaInput(
            tenantId,
            request.nome(),
            request.icone()
        );

        CategoriaDTO categoriaCriada = gerenciarCategoriasUseCase.criar(input);
        return Response.status(Response.Status.CREATED)
            .entity(categoriaCriada)
            .build();
    }

    @PUT
    @Path("/{id}")
    @RunOnVirtualThread
    public Response atualizarCategoria(@PathParam("id") UUID id,
                                       SalvarCategoriaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        AtualizarCategoriaInput input = new AtualizarCategoriaInput(
            tenantId,
            id,
            request.nome(),
            request.icone()
        );

        CategoriaDTO categoriaAtualizada = gerenciarCategoriasUseCase.atualizar(input);
        return Response.ok(categoriaAtualizada).build();
    }

    @DELETE
    @Path("/{id}")
    @RunOnVirtualThread
    public Response excluirCategoria(@PathParam("id") UUID id) {
        UUID tenantId = securityContext.getTenantId().valor();
        gerenciarCategoriasUseCase.excluir(tenantId, id);
        return Response.noContent().build();
    }
}
