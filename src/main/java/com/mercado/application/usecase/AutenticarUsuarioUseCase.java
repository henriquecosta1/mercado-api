package com.mercado.application.usecase;

import com.mercado.application.dto.LoginInput;
import com.mercado.application.dto.LoginOutput;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso: autentica um usuario validando login/senha via BCrypt e
 * emite um JWT SmallRye contendo as claims de tenant e perfil.
 *
 * Claims emitidas no token:
 *   - sub       : UUID do usuario
 *   - upn       : login do usuario
 *   - groups    : Set com o perfil (GERENTE ou OPERADOR)
 *   - tenant_id : UUID do tenant como String
 *   - nome      : nome completo do usuario
 */
@ApplicationScoped
public class AutenticarUsuarioUseCase {

    private static final Duration EXPIRACAO_PADRAO = Duration.ofHours(8);

    private final UsuarioRepository usuarioRepository;
    private final String issuer;

    @Inject
    public AutenticarUsuarioUseCase(
            UsuarioRepository usuarioRepository,
            @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "mercado-api") String issuer) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepository e obrigatorio.");
        this.issuer = issuer;
    }

    /**
     * Autentica o usuario e retorna um LoginOutput com o Bearer JWT assinado.
     *
     * @param input dados de login
     * @return LoginOutput com token assinado e metadados
     * @throws RegraDeNegocioException se credenciais invalidas ou usuario inativo
     */
    public LoginOutput executar(LoginInput input) {
        Objects.requireNonNull(input, "LoginInput nao pode ser nulo.");
        validarInput(input);

        String loginLimpo = input.login().trim().toLowerCase();

        java.util.List<Usuario> candidatos;
        if (input.tenantIdOpcional() != null) {
            candidatos = usuarioRepository.buscarPorLogin(loginLimpo, TenantId.de(input.tenantIdOpcional()))
                .map(java.util.List::of)
                .orElse(java.util.List.of());
        } else {
            candidatos = usuarioRepository.buscarPorLogin(loginLimpo);
        }

        if (candidatos.isEmpty()) {
            throw new RegraDeNegocioException("Credenciais invalidas.");
        }

        Usuario usuarioAutenticado = null;
        for (Usuario candidato : candidatos) {
            if (candidato.isAtivo() && candidato.autenticar(input.senha())) {
                usuarioAutenticado = candidato;
                break;
            }
        }

        if (usuarioAutenticado == null) {
            throw new RegraDeNegocioException("Credenciais invalidas.");
        }

        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(EXPIRACAO_PADRAO);

        String token = Jwt.issuer(issuer)
            .subject(usuarioAutenticado.getId().toString())
            .upn(usuarioAutenticado.getLogin())
            .groups(Set.of(usuarioAutenticado.getPerfil().name()))
            .claim("tenant_id", usuarioAutenticado.getTenantId().valor().toString())
            .claim("nome", usuarioAutenticado.getNome())
            .issuedAt(agora)
            .expiresAt(expiraEm)
            .sign();

        return new LoginOutput(
            token,
            usuarioAutenticado.getNome(),
            usuarioAutenticado.getPerfil().name(),
            usuarioAutenticado.getTenantId().valor(),
            expiraEm
        );
    }

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private void validarInput(LoginInput input) {
        if (input.login() == null || input.login().isBlank()) {
            throw new RegraDeNegocioException("Login é obrigatório.");
        }
        if (input.senha() == null || input.senha().isBlank()) {
            throw new RegraDeNegocioException("Senha é obrigatória.");
        }
    }

    private UUID resolverTenantId(LoginInput input) {
        return input.tenantIdOpcional() != null ? input.tenantIdOpcional() : DEFAULT_TENANT_ID;
    }
}