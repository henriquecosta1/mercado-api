package com.mercado.api.handler;

import com.mercado.domain.exception.RegraDeNegocioException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionHandlerTest {

    private DomainExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DomainExceptionHandler();
    }

    @Test
    void toResponse_DeveMascararExcecaoGenericaEGerarProtocoloParaHttp500() {
        // Arrange
        Exception excecaoGenerica = new RuntimeException("Detalhes confidenciais do banco de dados: connection refused");

        // Act
        Response response = handler.toResponse(excecaoGenerica);

        // Assert
        assertEquals(500, response.getStatus());
        
        Object entity = response.getEntity();
        assertTrue(entity instanceof Map, "A entidade da resposta deve ser um Map");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity;
        
        assertEquals(500, body.get("status"));
        assertEquals("Erro Interno", body.get("erro"));
        assertEquals("Ocorreu um erro interno no servidor. Tente novamente mais tarde ou contate o suporte.", body.get("mensagem"));
        assertNotNull(body.get("protocolo"), "Deve gerar um protocolo (UUID curto)");
        assertNotNull(body.get("timestamp"), "Deve conter o timestamp ISO");
        
        // Verifica se vazou o detalhe interno
        assertFalse(body.toString().contains("confidenciais do banco"), "O JSON nao pode conter detalhes internos da excecao");
    }

    @Test
    void toResponse_DeveManterMensagemDeRegraDeNegocioParaHttp422() {
        // Arrange
        RegraDeNegocioException excecaoNegocio = new RegraDeNegocioException("O cliente possui debito pendente.");

        // Act
        Response response = handler.toResponse(excecaoNegocio);

        // Assert
        assertEquals(422, response.getStatus());
        
        // Em RegraDeNegocio, ele retorna ErrorResponse ou algo serializavel que tenha a mensagem
        assertNotNull(response.getEntity());
        String bodyString = response.getEntity().toString();
        
        // Verifica se a mensagem segura foi exibida
        assertTrue(bodyString.contains("O cliente possui debito pendente."));
        assertTrue(bodyString.contains("Regra de Negocio Violada") || bodyString.contains("Regra de Neg\u00f3cio Violada"));
    }
}