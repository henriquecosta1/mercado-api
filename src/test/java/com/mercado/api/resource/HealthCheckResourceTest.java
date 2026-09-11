package com.mercado.api.resource;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Unidade: HealthCheckResource")
class HealthCheckResourceTest {

    @Test
    @DisplayName("Deve retornar status 200 OK com payload UP e timestamp atual")
    void deveRetornarStatusUpETimestamp() {
        HealthCheckResource resource = new HealthCheckResource();

        long antes = System.currentTimeMillis();
        Response response = resource.check();
        long depois = System.currentTimeMillis();

        assertNotNull(response);
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();

        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertNotNull(body.get("timestamp"));

        long timestamp = (long) body.get("timestamp");
        assertTrue(timestamp >= antes && timestamp <= depois);
    }
}
