package com.mercado.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Alias REST mapeado sob /usuarios, garantindo compatibilidade com chamadas
 * com ou sem o prefixo /api.
 */
@Path("/usuarios")
public class UsuariosResource extends UsuarioResource {

    @Inject
    public UsuariosResource(JsonWebToken jwt) {
        super(jwt);
    }
}