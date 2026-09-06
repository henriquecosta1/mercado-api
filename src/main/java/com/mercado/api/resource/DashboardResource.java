package com.mercado.api.resource;

import com.mercado.application.dto.DashboardResumoOutput;
import com.mercado.application.usecase.ObterDashboardResumoUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para o Dashboard Gerencial e Métricas Comerciais.
 * Mapeado sob /dashboard.
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final ObterDashboardResumoUseCase obterDashboardResumoUseCase;

    @Inject
    public DashboardResource(ObterDashboardResumoUseCase obterDashboardResumoUseCase) {
        this.obterDashboardResumoUseCase = Objects.requireNonNull(obterDashboardResumoUseCase, "ObterDashboardResumoUseCase é obrigatório.");
    }

    @GET
    @Path("/resumo")
    @RunOnVirtualThread
    public Response obterResumo(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        DashboardResumoOutput output = obterDashboardResumoUseCase.executar(tenantId);
        return Response.ok(output).build();
    }

    protected UUID extrairTenantId(String tenantIdHeader) {
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
