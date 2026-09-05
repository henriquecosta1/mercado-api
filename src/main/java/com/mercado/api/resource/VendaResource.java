package com.mercado.api.resource;

import com.mercado.api.dto.NovaVendaRequest;
import com.mercado.api.dto.VendaResponse;
import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.application.usecase.RegistrarVendaUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para operações de Venda.
 * Mapeado em /vendas (com prefixo /api configurado no application.properties, resultando em /api/vendas).
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 */
@Path("/api/vendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendaResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final RegistrarVendaUseCase registrarVendaUseCase;

    @Inject
    public VendaResource(RegistrarVendaUseCase registrarVendaUseCase) {
        this.registrarVendaUseCase = Objects.requireNonNull(registrarVendaUseCase, "RegistrarVendaUseCase é obrigatório.");
    }

    @POST
    @RunOnVirtualThread
    public Response criarVenda(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                               NovaVendaRequest request) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            throw new IllegalArgumentException("O cabeçalho obrigatório '" + HEADER_TENANT_ID + "' não foi informado.");
        }

        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Identificador de Tenant ('" + HEADER_TENANT_ID + "') com formato UUID inválido: " + tenantIdHeader);
        }

        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantId,
            request.valorTotal(),
            request.valorRecebido(),
            request.formaPagamento(),
            request.nomeClienteFiado(),
            request.telefoneClienteFiado(),
            request.descricao()
        );

        RegistrarVendaOutput output = registrarVendaUseCase.executar(input);

        return Response.status(Response.Status.CREATED)
            .entity(VendaResponse.from(output))
            .build();
    }
}
