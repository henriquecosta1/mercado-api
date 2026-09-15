package com.mercado.api.resource;

import com.mercado.api.dto.AlterarSenhaRequest;
import com.mercado.infrastructure.persistence.entity.UsuarioJpaEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.UUID;

/**
 * Recurso REST para operações de Usuários.
 * Mapeado sob /api/usuarios.
 */
@Authenticated
@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    protected final JsonWebToken jwt;

    @Inject
    public UsuarioResource(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    @PUT
    @Path("/alterar-senha")
    @Transactional
    @RunOnVirtualThread
    public Response alterarSenha(AlterarSenhaRequest request) {
        if (request == null || request.senhaAtual() == null || request.senhaAtual().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("erro", "A senha atual é obrigatória."))
                .build();
        }

        if (request.novaSenha() == null || request.novaSenha().isBlank() || request.novaSenha().trim().length() < 6) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("erro", "A nova senha deve ter no mínimo 6 caracteres."))
                .build();
        }

        String subject = jwt.getSubject();
        String login = jwt.getName();

        if ((subject == null || subject.isBlank()) && (login == null || login.isBlank())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("erro", "Usuário não identificado no token JWT."))
                .build();
        }

        UsuarioJpaEntity usuario = null;

        if (subject != null && !subject.isBlank()) {
            try {
                UUID usuarioId = UUID.fromString(subject.trim());
                usuario = UsuarioJpaEntity.findById(usuarioId);
            } catch (IllegalArgumentException ignored) {
                // Fallback para login
            }
        }

        if (usuario == null && login != null && !login.isBlank()) {
            usuario = UsuarioJpaEntity.find("login = ?1 and ativo = true", login.trim().toLowerCase()).firstResult();
        }

        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("erro", "Usuário não encontrado."))
                .build();
        }

        try {
            if (!BCrypt.checkpw(request.senhaAtual(), usuario.senhaHash)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", "A senha atual informada está incorreta."))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("erro", "A senha atual informada está incorreta."))
                .build();
        }

        usuario.senhaHash = BCrypt.hashpw(request.novaSenha().trim(), BCrypt.gensalt(12));
        usuario.persist();

        return Response.ok(Map.of("mensagem", "Senha alterada com sucesso.")).build();
    }
}