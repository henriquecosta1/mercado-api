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
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
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
 * Recurso REST para operacoes de Venda.
 * Mapeado em /api/vendas.
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/api/vendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendaResource {

    /** @deprecated Mantido por compatibilidade de integracao; use JWT claims. */
    @Deprecated
    public static final String HEADER_GERENTE_PIN = "X-Gerente-Pin";

    private final RegistrarVendaUseCase registrarVendaUseCase;
    private final ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase;
    private final CancelarVendaUseCase cancelarVendaUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public VendaResource(RegistrarVendaUseCase registrarVendaUseCase,
                         ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase,
                         CancelarVendaUseCase cancelarVendaUseCase,
                         TenantSecurityContext securityContext) {
        this.registrarVendaUseCase = Objects.requireNonNull(registrarVendaUseCase, "RegistrarVendaUseCase e obrigatorio.");
        this.listarVendasCaixaAtualUseCase = Objects.requireNonNull(listarVendasCaixaAtualUseCase, "ListarVendasCaixaAtualUseCase e obrigatorio.");
        this.cancelarVendaUseCase = Objects.requireNonNull(cancelarVendaUseCase, "CancelarVendaUseCase e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @POST
    @RunOnVirtualThread
    public Response criarVenda(NovaVendaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();

        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisicao nao pode ser vazio.");
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
    public Response listarVendasCaixaAtual() {
        UUID tenantId = securityContext.getTenantId().valor();
        List<VendaResumoDTO> vendas = listarVendasCaixaAtualUseCase.executar(tenantId);
        return Response.ok(vendas).build();
    }

    @POST
    @Path("/{id}/cancelar")
    @RunOnVirtualThread
    public Response cancelarVenda(@HeaderParam(HEADER_GERENTE_PIN) String gerentePin,
                                  @QueryParam("pin") String pinQuery,
                                  @PathParam("id") UUID vendaId,
                                  CancelarVendaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (vendaId == null) {
            throw new IllegalArgumentException("Id da venda e obrigatorio.");
        }
        String motivo = request != null ? request.motivo() : null;
        String pinEfetivo = (gerentePin != null && !gerentePin.isBlank()) ? gerentePin.trim() : null;
        if (pinEfetivo == null && request != null) {
            String pinCorpo = (request.pin() != null && !request.pin().isBlank()) ? request.pin() : request.pinGerente();
            if (pinCorpo != null && !pinCorpo.isBlank()) {
                pinEfetivo = pinCorpo.trim();
            }
        }
        if (pinEfetivo == null && pinQuery != null && !pinQuery.isBlank()) {
            pinEfetivo = pinQuery.trim();
        }

        CancelarVendaInput input = new CancelarVendaInput(tenantId, vendaId, motivo, pinEfetivo);
        CancelarVendaOutput output = cancelarVendaUseCase.executar(input);
        return Response.ok(output).build();
    }
}