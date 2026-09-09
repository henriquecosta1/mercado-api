package com.mercado.api.resource;

import com.mercado.application.usecase.ObterDashboardResumoUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST mapeado sob /api/dashboard/resumo, mantendo total compatibilidade
 * com clientes frontend que utilizam o prefixo de rota /api.
 */
@Path("/api/dashboard")
public class ApiDashboardResource extends DashboardResource {

    @Inject
    public ApiDashboardResource(ObterDashboardResumoUseCase obterDashboardResumoUseCase,
                                TenantSecurityContext securityContext) {
        super(obterDashboardResumoUseCase, securityContext);
    }
}