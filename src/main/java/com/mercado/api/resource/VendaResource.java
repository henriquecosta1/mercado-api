package com.mercado.api.resource;

import com.mercado.api.dto.CancelarVendaRequest;
import com.mercado.api.dto.NovaVendaRequest;
import com.mercado.api.dto.VendaResponse;
import com.mercado.application.dto.CancelarVendaInput;
import com.mercado.application.dto.CancelarVendaOutput;
import com.mercado.application.dto.ItemVendaInput;
import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.application.dto.VendaResumoDTO;
import com.mercado.application.usecase.CancelarVendaUseCase;
import com.mercado.application.usecase.ListarVendasCaixaAtualUseCase;
import com.mercado.application.usecase.RegistrarVendaUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para operações de Venda.
 * Mapeado em /api/vendas.
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 */
@Path("/api/vendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendaResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final RegistrarVendaUseCase registrarVendaUseCase;
    private final ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase;
    private final CancelarVendaUseCase cancelarVendaUseCase;

    @Inject
    public VendaResource(RegistrarVendaUseCase registrarVendaUseCase,
                         ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase,
                         CancelarVendaUseCase cancelarVendaUseCase) {
        this.registrarVendaUseCase = Objects.requireNonNull(registrarVendaUseCase, "RegistrarVendaUseCase é obrigatório.");
        this.listarVendasCaixaAtualUseCase = Objects.requireNonNull(listarVendasCaixaAtualUseCase, "ListarVendasCaixaAtualUseCase é obrigatório.");
        this.cancelarVendaUseCase = Objects.requireNonNull(cancelarVendaUseCase, "CancelarVendaUseCase é obrigatório.");
    }

    @POST
    @RunOnVirtualThread
    public Response criarVenda(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                               NovaVendaRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);

        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        List<ItemVendaInput> itensInput = request.itens() != null
            ? request.itens().stream()
                .map(i -> new ItemVendaInput(i.produtoId(), i.descricao(), i.quantidade(), i.precoUnitario()))
                .toList()
            : null;

        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantId,
            request.valorTotal(),
            request.valorRecebido(),
            request.formaPagamento(),
            request.nomeClienteFiado(),
            request.telefoneClienteFiado(),
            request.descricao(),
            itensInput
        );

        RegistrarVendaOutput output = registrarVendaUseCase.executar(input);

        return Response.status(Response.Status.CREATED)
            .entity(VendaResponse.from(output))
            .build();
    }

    @GET
    @Path("/caixa-atual")
    @RunOnVirtualThread
    public Response listarVendasCaixaAtual(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        List<VendaResumoDTO> vendas = listarVendasCaixaAtualUseCase.executar(tenantId);
        return Response.ok(vendas).build();
    }

    @POST
    @Path("/{id}/cancelar")
    @RunOnVirtualThread
    public Response cancelarVenda(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                  @PathParam("id") UUID vendaId,
                                  CancelarVendaRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (vendaId == null) {
            throw new IllegalArgumentException("Id da venda é obrigatório.");
        }
        String motivo = request != null ? request.motivo() : null;
        CancelarVendaInput input = new CancelarVendaInput(tenantId, vendaId, motivo);
        CancelarVendaOutput output = cancelarVendaUseCase.executar(input);
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
