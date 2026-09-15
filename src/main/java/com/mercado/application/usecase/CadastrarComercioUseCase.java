package com.mercado.application.usecase;

import com.mercado.application.dto.CadastroComercioInput;
import com.mercado.application.dto.CadastroComercioOutput;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Usuario;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Caso de Uso: Auto-cadastro público de novos estabelecimentos (Comércios).
 * Cria a loja (Tenant) em estado PENDENTE para aprovação posterior do administrador
 * e registra o primeiro usuário com perfil de GERENTE.
 */
@ApplicationScoped
public class CadastrarComercioUseCase {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    @Inject
    public CadastrarComercioUseCase(TenantRepository tenantRepository,
                                    UsuarioRepository usuarioRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository é obrigatório.");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepository é obrigatório.");
    }

    @Transactional
    public CadastroComercioOutput executar(CadastroComercioInput input) {
        Objects.requireNonNull(input, "Dados de cadastro não podem ser nulos.");
        validarInput(input);

        String loginLimpo = input.login().trim().toLowerCase();

        // 1. Validação de unicidade de login no sistema
        
        if (usuarioRepository.buscarPorLogin(loginLimpo).isPresent()) {
            throw new RegraDeNegocioException("Login já está em uso por outro usuário.");
        }

        // 2. Criação do Tenant em estado PENDENTE
        Tenant tenant = Tenant.autoCadastrar(
            input.nomeComercio(),
            input.whatsapp(),
            input.pinGerenteOpcional()
        );
        tenantRepository.salvar(tenant);

        // 3. Criação do primeiro usuário Gerente
        Usuario gerente = Usuario.criar(
            tenant.getId(),
            input.nomeGerente().trim(),
            loginLimpo,
            input.senha(),
            Usuario.Perfil.GERENTE
        );
        usuarioRepository.salvar(gerente);

        return new CadastroComercioOutput(
            tenant.getId().valor(),
            tenant.getNome(),
            tenant.getStatus().name(),
            "Cadastro realizado com sucesso! Aguarde a liberação do administrador."
        );
    }

    private void validarInput(CadastroComercioInput input) {
        if (input.nomeComercio() == null || input.nomeComercio().isBlank()) {
            throw new RegraDeNegocioException("Nome do comércio é obrigatório.");
        }
        if (input.whatsapp() == null || input.whatsapp().isBlank()) {
            throw new RegraDeNegocioException("WhatsApp de contato é obrigatório.");
        }
        if (input.nomeGerente() == null || input.nomeGerente().isBlank()) {
            throw new RegraDeNegocioException("Nome do gerente é obrigatório.");
        }
        if (input.login() == null || input.login().isBlank()) {
            throw new RegraDeNegocioException("Login de acesso é obrigatório.");
        }
        if (input.senha() == null || input.senha().isBlank()) {
            throw new RegraDeNegocioException("Senha de acesso é obrigatória.");
        }
        if (input.senha().trim().length() < 4) {
            throw new RegraDeNegocioException("A senha deve ter no mínimo 4 caracteres.");
        }
    }
}
