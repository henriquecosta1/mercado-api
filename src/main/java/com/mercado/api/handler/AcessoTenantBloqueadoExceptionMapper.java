package com.mercado.api.handler;

import com.mercado.domain.exception.AcessoTenantBloqueadoException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * ExceptionMapper para AcessoTenantBloqueadoException.
 * Retorna HTTP 403 Forbidden com payload JSON contendo o código de bloqueio e a mensagem amigável.
 */
@Provider
public class AcessoTenantBloqueadoExceptionMapper implements ExceptionMapper<AcessoTenantBloqueadoException> {

    @Override
    public Response toResponse(AcessoTenantBloqueadoException exception) {
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(Map.of(
                "codigo", exception.getCodigo(),
                "mensagem", exception.getMensagem()
            ))
            .build();
    }
}
