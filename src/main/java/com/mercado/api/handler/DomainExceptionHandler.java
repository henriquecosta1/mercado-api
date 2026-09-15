package com.mercado.api.handler;

import com.mercado.api.dto.ErrorResponse;
import com.mercado.domain.exception.DomainException;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof RecursoNaoEncontradoException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ErrorResponse.of("Recurso Não Encontrado", e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        }

        if (exception instanceof com.mercado.domain.exception.AcessoTenantBloqueadoException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .entity(java.util.Map.of(
                    "codigo", e.getCodigo(),
                    "mensagem", e.getMensagem()
                ))
                .build();
        }

        if (exception instanceof com.mercado.domain.exception.PinInvalidoException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(ErrorResponse.of("Acesso Negado", e.getMessage(), Response.Status.FORBIDDEN.getStatusCode()))
                .build();
        }

        if (exception instanceof com.mercado.domain.exception.ClienteBloqueadoException e) {
            return Response.status(422)
                .entity(ErrorResponse.of("Cliente Bloqueado", e.getMessage(), 422))
                .build();
        }

        if (exception instanceof com.mercado.domain.exception.LimiteCreditoExcedidoException e) {
            return Response.status(422)
                .entity(ErrorResponse.of("Limite de Crédito Excedido", e.getMessage(), 422))
                .build();
        }

        if (exception instanceof com.mercado.domain.exception.ClienteComDebitoException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.of("Cliente com Débito Pendente", e.getMessage(), Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        }

        if (exception instanceof RegraDeNegocioException e) {
            return Response.status(422)
                .entity(ErrorResponse.of("Regra de Negócio Violada", e.getMessage(), 422))
                .build();
        }

        if (exception instanceof DomainException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.of("Erro de Domínio", e.getMessage(), Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        }

                if (exception instanceof IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.of("Requisicao Invalida", e.getMessage(), Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        }

        // --- TRATAMENTO GLOBAL DE CONSTRAINT VIOLATION ---
        if (exception instanceof jakarta.persistence.PersistenceException || exception instanceof org.hibernate.exception.ConstraintViolationException || (exception.getCause() != null && exception.getCause() instanceof org.hibernate.exception.ConstraintViolationException)) {
            String msg = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
            if (msg.contains("constraint") || msg.contains("violates foreign key constraint")) {
                return Response.status(422)
                    .entity(ErrorResponse.of("Regra de Negocio Violada", "Nao e possivel excluir este registro pois ele ja possui historico vinculado no sistema. Tente inativa-lo.", 422))
                    .build();
            }
        }
        
        // Outro fallback
        if (exception.getMessage() != null && exception.getMessage().contains("violates foreign key constraint")) {
            return Response.status(422)
                .entity(ErrorResponse.of("Regra de Negocio Violada", "Nao e possivel excluir este registro pois ele ja possui historico vinculado no sistema. Tente inativa-lo.", 422))
                .build();
        }

        if (exception instanceof jakarta.ws.rs.WebApplicationException e) {
            return e.getResponse();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ErrorResponse.of("Erro Interno", exception.getMessage() != null ? exception.getMessage() : "Ocorreu um erro interno inesperado.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
            .build();
    }
}
