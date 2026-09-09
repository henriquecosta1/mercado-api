package com.mercado.api.resource;

import com.mercado.application.usecase.GerenciarCategoriasUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST mapeado sob /api/categorias, mantendo compatibilidade com clientes
 * que utilizam o prefixo /api (ex: Vite proxy).
 */
@Path("/api/categorias")
public class ApiCategoriaResource extends CategoriaResource {

    @Inject
    public ApiCategoriaResource(GerenciarCategoriasUseCase gerenciarCategoriasUseCase,
                                TenantSecurityContext securityContext) {
        super(gerenciarCategoriasUseCase, securityContext);
    }
}
