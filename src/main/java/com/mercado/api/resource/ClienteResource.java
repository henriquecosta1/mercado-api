package com.mercado.api.resource;

import com.mercado.api.dto.AmortizacaoRequest;
import com.mercado.application.dto.AmortizarFiadoInput;
import com.mercado.application.dto.AmortizarFiadoOutput;
import com.mercado.application.dto.ClienteDTO;
import com.mercado.application.usecase.AmortizarFiadoUseCase;
import com.mercado.application.usecase.ListarClientesFiadoUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
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
 * Recurso REST para operações relacionadas a Clientes e Fiado.
 * Mapeado sob o prefixo /api/clientes.
 * Executa em Virtual Threads do Java 21 via @RunOnVirtualThread.
 */
@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final ListarClientesFiadoUseCase listarClientesFiadoUseCase;
    private final AmortizarFiadoUseCase amortizarFiadoUseCase;

    @Inject
    public ClienteResource(ListarClientesFiadoUseCase listarClientesFiadoUseCase,
                           AmortizarFiadoUseCase amortizarFiadoUseCase) {
        this.listarClientesFiadoUseCase = Objects.requireNonNull(listarClientesFiadoUseCase, "ListarClientesFiadoUseCase é obrigatório.");
        this.amortizarFiadoUseCase = Objects.requireNonNull(amortizarFiadoUseCase, "AmortizarFiadoUseCase é obrigatório.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarClientesFiado(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                        @QueryParam("busca") String busca) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        List<ClienteDTO> clientes = listarClientesFiadoUseCase.executar(tenantId, busca);
        return Response.ok(clientes).build();
    }

    @POST
    @Path("/{id}/amortizacoes")
    @RunOnVirtualThread
    public Response amortizarFiado(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                   @PathParam("id") UUID clienteId,
                                   AmortizacaoRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);

        if (clienteId == null) {
            throw new IllegalArgumentException("Identificador do cliente é obrigatório na URL.");
        }
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantId,
            clienteId,
            request.valorPago(),
            request.formaPagamento()
        );

        AmortizarFiadoOutput output = amortizarFiadoUseCase.executar(input);
        return Response.ok(output).build();
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
