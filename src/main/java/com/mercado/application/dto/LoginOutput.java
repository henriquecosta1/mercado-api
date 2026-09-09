package com.mercado.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de saida da autenticacao bem-sucedida.
 *
 * @param token    Bearer JWT gerado
 * @param nome     nome do usuario autenticado
 * @param perfil   perfil de acesso (GERENTE ou OPERADOR)
 * @param tenantId UUID do tenant ao qual o usuario pertence
 * @param expiraEm instante de expiracao do token
 */
public record LoginOutput(
    String token,
    String nome,
    String perfil,
    UUID tenantId,
    Instant expiraEm
) {
}