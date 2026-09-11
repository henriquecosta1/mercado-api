package com.mercado.api.resource;

import com.mercado.application.dto.DashboardResumoOutput;
import com.mercado.application.usecase.ObterDashboardResumoUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para o Dashboard Gerencial e Metricas Comerciais.
 * Mapeado sob /dashboard.
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardResource {

    private final ObterDashboardResumoUseCase obterDashboardResumoUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public DashboardResource(ObterDashboardResumoUseCase obterDashboardResumoUseCase,
                             TenantSecurityContext securityContext) {
        this.obterDashboardResumoUseCase = Objects.requireNonNull(obterDashboardResumoUseCase, "ObterDashboardResumoUseCase e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @GET
    @RunOnVirtualThread
    public Response obterDashboard(@QueryParam("periodo") String periodo,
                                  @QueryParam("inicio") LocalDate inicio,
                                  @QueryParam("fim") LocalDate fim) {
        return obterResumo(periodo, inicio, fim);
    }

    @GET
    @Path("/resumo")
    @RunOnVirtualThread
    public Response obterResumo(@QueryParam("periodo") String periodo,
                                @QueryParam("inicio") LocalDate inicio,
                                @QueryParam("fim") LocalDate fim) {
        UUID tenantId = securityContext.getTenantId().valor();
        DashboardResumoOutput output = obterDashboardResumoUseCase.executar(tenantId, periodo, inicio, fim, ZoneId.systemDefault());
        return Response.ok(output).build();
    }
}