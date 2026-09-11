package com.mercado.api.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Endpoint de Health Check público e ultra-leve para monitoramento de disponibilidade da API.
 * Mapeado em /api/health. Não exige autenticação JWT (@PermitAll).
 */
@Path("/api/health")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource {

    @GET
    public Response check() {
        return Response.ok(Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis()
        )).build();
    }
}
