package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.TenantId;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica que representa o Usuario do sistema (operador ou gerente).
 * Totalmente desacoplada de frameworks.
 */
public class Usuario {

    public enum Perfil {
        OPERADOR, GERENTE
    }

    private final UUID id;
    private final TenantId tenantId;
    private String nome;
    private final String login;
    private String senhaHash;
    private Perfil perfil;
    private boolean ativo;
    private final Instant criadoEm;

    public Usuario(UUID id,
                   TenantId tenantId,
                   String nome,
                   String login,
                   String senhaHash,
                   Perfil perfil,
                   boolean ativo,
                   Instant criadoEm) {
        this.id = Objects.requireNonNull(id, "Id do usuario nao pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId nao pode ser nulo.");
        this.nome = validarNome(nome);
        this.login = validarLogin(login);
        this.senhaHash = Objects.requireNonNull(senhaHash, "Senha hash nao pode ser nula.");
        this.perfil = Objects.requireNonNull(perfil, "Perfil nao pode ser nulo.");
        this.ativo = ativo;
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criacao nao pode ser nula.");
    }

    /**
     * Cria um novo usuario com senha em texto puro (aplica hash BCrypt custo 12).
     */
    public static Usuario criar(TenantId tenantId, String nome, String login, String senhaTexto, Perfil perfil) {
        Objects.requireNonNull(senhaTexto, "Senha nao pode ser nula.");
        if (senhaTexto.isBlank()) {
            throw new RegraDeNegocioException("Senha nao pode ser vazia.");
        }
        String hash = BCrypt.hashpw(senhaTexto, BCrypt.gensalt(12));
        return new Usuario(UUID.randomUUID(), tenantId, nome, login, hash, perfil, true, Instant.now());
    }

    /**
     * Verifica as credenciais do usuario com BCrypt.
     *
     * @param senhaTexto senha em texto puro fornecida na autenticacao
     * @return true se a senha corresponde ao hash armazenado
     */
    public boolean autenticar(String senhaTexto) {
        if (senhaTexto == null || senhaTexto.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(senhaTexto, this.senhaHash);
    }

    /**
     * Inativa o usuario impedindo futuros logins.
     */
    public void inativar() {
        this.ativo = false;
    }

    /**
     * Reativa o usuario.
     */
    public void ativar() {
        this.ativo = true;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do usuario e obrigatorio.");
        }
        return nome.trim();
    }

    private static String validarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new RegraDeNegocioException("Login do usuario e obrigatorio.");
        }
        return login.trim().toLowerCase();
    }

    // Getters
    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}