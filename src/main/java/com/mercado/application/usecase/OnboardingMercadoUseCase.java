package com.mercado.application.usecase;

import com.mercado.application.dto.LoginOutput;
import com.mercado.application.dto.RegistrarMercadoInput;
import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CategoriaProdutoRepository;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Caso de Uso: Onboarding público de novos Mercados.
 * Cria o Tenant, o Usuário Administrador (GERENTE), as categorias padrão da loja
 * e emite o token JWT para login automático imediato.
 */
@ApplicationScoped
public class OnboardingMercadoUseCase {

    private static final Duration EXPIRACAO_PADRAO = Duration.ofHours(8);

    private static final List<CategoriaPadrao> CATEGORIAS_PADRAO = List.of(
        new CategoriaPadrao("Mercearia", "cart"),
        new CategoriaPadrao("Bebidas", "beer"),
        new CategoriaPadrao("Frios & Laticínios", "cheese"),
        new CategoriaPadrao("Hortifrúti", "apple"),
        new CategoriaPadrao("Padaria", "bread"),
        new CategoriaPadrao("Limpeza", "sparkles"),
        new CategoriaPadrao("Higiene", "heart")
    );

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaProdutoRepository categoriaProdutoRepository;
    private final String issuer;

    @Inject
    public OnboardingMercadoUseCase(
            TenantRepository tenantRepository,
            UsuarioRepository usuarioRepository,
            CategoriaProdutoRepository categoriaProdutoRepository,
            @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "mercado-api") String issuer) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository é obrigatório.");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepository é obrigatório.");
        this.categoriaProdutoRepository = Objects.requireNonNull(categoriaProdutoRepository, "CategoriaProdutoRepository é obrigatório.");
        this.issuer = issuer;
    }

    @Transactional
    public LoginOutput executar(RegistrarMercadoInput input) {
        Objects.requireNonNull(input, "Dados de registro do mercado não podem ser nulos.");
        validarInput(input);

        // 1. Cria e persiste o novo Tenant
        Tenant tenant = Tenant.criar(input.nomeMercado(), input.pinGerente());
        tenantRepository.salvar(tenant);

        // 2. Cria e persiste o Usuário Gerente
        Usuario admin = Usuario.criar(
            tenant.getId(),
            input.nomeResponsavel(),
            input.login(),
            input.senha(),
            Usuario.Perfil.GERENTE
        );
        usuarioRepository.salvar(admin);

        // 3. Insere as categorias padrão do catálogo para o novo tenant
        for (CategoriaPadrao cat : CATEGORIAS_PADRAO) {
            CategoriaProduto categoria = CategoriaProduto.criar(tenant.getId(), cat.nome(), cat.icone());
            categoriaProdutoRepository.salvar(categoria);
        }

        // 4. Emite token JWT para login automático
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(EXPIRACAO_PADRAO);

        String token = Jwt.issuer(issuer)
            .subject(admin.getId().toString())
            .upn(admin.getLogin())
            .groups(Set.of(admin.getPerfil().name()))
            .claim("tenant_id", admin.getTenantId().valor().toString())
            .claim("nome", admin.getNome())
            .claim("nome_mercado", tenant.getNome())
            .issuedAt(agora)
            .expiresAt(expiraEm)
            .sign();

        return new LoginOutput(
            token,
            admin.getNome(),
            admin.getPerfil().name(),
            admin.getTenantId().valor(),
            tenant.getNome(),
            expiraEm
        );
    }

    private void validarInput(RegistrarMercadoInput input) {
        if (input.nomeMercado() == null || input.nomeMercado().isBlank()) {
            throw new RegraDeNegocioException("Nome do mercado é obrigatório.");
        }
        if (input.nomeResponsavel() == null || input.nomeResponsavel().isBlank()) {
            throw new RegraDeNegocioException("Nome do responsável é obrigatório.");
        }
        if (input.login() == null || input.login().isBlank()) {
            throw new RegraDeNegocioException("Login de acesso é obrigatório.");
        }
        if (input.senha() == null || input.senha().isBlank()) {
            throw new RegraDeNegocioException("Senha é obrigatória.");
        }
    }

    private record CategoriaPadrao(String nome, String icone) {}
}
