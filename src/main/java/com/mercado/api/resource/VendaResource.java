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
import org.jboss.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(VendaResource.class);

    /** @deprecated Mantido por compatibilidade de integracao; use JWT claims. */
    @Deprecated
    public static final String HEADER_GERENTE_PIN = "X-Gerente-Pin";

    private final RegistrarVendaUseCase registrarVendaUseCase;
    private final ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase;
    private final com.mercado.application.usecase.ListarVendasUseCase listarVendasUseCase;
    private final CancelarVendaUseCase cancelarVendaUseCase;
    private final com.mercado.application.usecase.ObterDetalhesVendaUseCase obterDetalhesVendaUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public VendaResource(RegistrarVendaUseCase registrarVendaUseCase,
                         ListarVendasCaixaAtualUseCase listarVendasCaixaAtualUseCase,
                         com.mercado.application.usecase.ListarVendasUseCase listarVendasUseCase,
                         CancelarVendaUseCase cancelarVendaUseCase,
                         com.mercado.application.usecase.ObterDetalhesVendaUseCase obterDetalhesVendaUseCase,
                         TenantSecurityContext securityContext) {
        this.registrarVendaUseCase = Objects.requireNonNull(registrarVendaUseCase, "RegistrarVendaUseCase e obrigatorio.");
        this.listarVendasCaixaAtualUseCase = Objects.requireNonNull(listarVendasCaixaAtualUseCase, "ListarVendasCaixaAtualUseCase e obrigatorio.");
        this.listarVendasUseCase = Objects.requireNonNull(listarVendasUseCase, "ListarVendasUseCase e obrigatorio.");
        this.cancelarVendaUseCase = Objects.requireNonNull(cancelarVendaUseCase, "CancelarVendaUseCase e obrigatorio.");
        this.obterDetalhesVendaUseCase = Objects.requireNonNull(obterDetalhesVendaUseCase, "ObterDetalhesVendaUseCase e obrigatorio.");
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
    @RunOnVirtualThread
    public Response listarVendas(@QueryParam("caixaId") UUID caixaId,
                                 @QueryParam("status") String status,
                                 @QueryParam("de") String de,
                                 @QueryParam("ate") String ate,
                                 @QueryParam("page") @jakarta.ws.rs.DefaultValue("0") int page,
                                 @QueryParam("size") @jakarta.ws.rs.DefaultValue("10") int size) {
        UUID tenantId = securityContext.getTenantId().valor();
                java.time.Instant dataDe = (de != null && !de.isBlank()) ? java.time.Instant.parse(de.trim()) : java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        java.time.Instant dataAte = (ate != null && !ate.isBlank()) ? java.time.Instant.parse(ate.trim()) : java.time.LocalDate.now().atTime(23, 59, 59, 999_999_999).atZone(java.time.ZoneId.systemDefault()).toInstant();
        

        com.mercado.application.dto.PageDTO<VendaResumoDTO> resultado = listarVendasUseCase.executar(
            tenantId, caixaId, status, dataDe, dataAte, page, size
        );
        return Response.ok(resultado).build();
    }

        @GET
    @Path("/do-dia")
    @RunOnVirtualThread
    public Response listarVendasDoDia(@QueryParam("page") Integer page,
                                      @QueryParam("size") Integer size) {
        UUID tenantId = securityContext.getTenantId().valor();
        int pagina = page != null ? page : 0;
        int tamanho = size != null ? size : 100;
        
        java.time.Instant dataDe = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        java.time.Instant dataAte = java.time.LocalDate.now().atTime(23, 59, 59, 999_999_999).atZone(java.time.ZoneId.systemDefault()).toInstant();
        
        com.mercado.application.dto.PageDTO<VendaResumoDTO> paginado = listarVendasUseCase.executar(
            tenantId, null, null, dataDe, dataAte, pagina, tamanho
        );
        return Response.ok(paginado).build();
    }

    @GET
    @Path("/caixa-atual")
    @RunOnVirtualThread
    public Response listarVendasCaixaAtual(@QueryParam("page") Integer page,
                                          @QueryParam("size") Integer size) {
        // Redireciona para do-dia para evitar zerar quando o caixa fecha
        return listarVendasDoDia(page, size);
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response obterDetalhesVenda(@PathParam("id") UUID id) {
        UUID tenantId = securityContext.getTenantId().valor();
        com.mercado.application.dto.VendaDetalheResponseDTO detalhe = obterDetalhesVendaUseCase.executar(tenantId, id);
        return Response.ok(detalhe).build();
    }

        @POST
    @Path("/{id}/cancelar")
    @RunOnVirtualThread
    public Response cancelarVenda(@HeaderParam("X-Gerente-Pin") String pinHeader,
                                  @PathParam("id") UUID vendaId,
                                  CancelarVendaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (vendaId == null) {
            throw new IllegalArgumentException("Id da venda e obrigatorio.");
        }
        
        String motivo = request != null ? request.motivo() : null;
        String pinEfetivo = (request != null && request.pin() != null && !request.pin().isBlank()) 
                            ? request.pin().trim() 
                            : (request != null && request.pinGerente() != null && !request.pinGerente().isBlank() 
                                ? request.pinGerente().trim() 
                                : null);

        if (pinEfetivo == null && pinHeader != null && !pinHeader.isBlank()) {
            pinEfetivo = pinHeader.trim();
        }
        
        if (pinEfetivo == null || pinEfetivo.isBlank()) {
            throw new jakarta.ws.rs.WebApplicationException(
                jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("erro", "Autorizacao de gerente obrigatoria: PIN nao fornecido no corpo ou cabecalho."))
                    .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build()
            );
        }

        CancelarVendaInput input = new CancelarVendaInput(tenantId, vendaId, motivo, pinEfetivo);
                CancelarVendaOutput output = cancelarVendaUseCase.executar(input);
        
        LOG.infof("Venda cancelada com sucesso | VendaID: %s | Operador: %s | Motivo: %s", 
                  vendaId, securityContext.getLogin(), motivo);
        return Response.ok(output).build();
    }
}