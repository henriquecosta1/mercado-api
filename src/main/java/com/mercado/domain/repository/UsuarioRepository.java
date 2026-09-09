package com.mercado.domain.repository;

import com.mercado.domain.entity.Usuario;
import com.mercado.domain.valueobject.TenantId;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de repositorio para a entidade Usuario.
 * Definido na camada de Dominio, implementado na Infraestrutura.
 */
public interface UsuarioRepository {

    /**
     * Busca um usuario pelo login dentro de um tenant especifico.
     *
     * @param login    login do usuario (case-insensitive)
     * @param tenantId identificador do tenant
     * @return Optional com o usuario, ou vazio se nao encontrado
     */
    Optional<Usuario> buscarPorLogin(String login, TenantId tenantId);

    /**
     * Busca todos os usuarios com determinado login em qualquer tenant.
     *
     * @param login login do usuario (case-insensitive)
     * @return Lista com os usuarios encontrados
     */
    java.util.List<Usuario> buscarPorLogin(String login);

    /**
     * Busca um usuario pelo seu ID dentro de um tenant especifico.
     *
     * @param id       UUID do usuario
     * @param tenantId identificador do tenant
     * @return Optional com o usuario, ou vazio se nao encontrado
     */
    Optional<Usuario> buscarPorId(UUID id, TenantId tenantId);

    /**
     * Persiste ou atualiza um usuario.
     *
     * @param usuario entidade a ser salva
     */
    void salvar(Usuario usuario);
}