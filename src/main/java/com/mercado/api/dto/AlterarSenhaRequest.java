package com.mercado.api.dto;

public record AlterarSenhaRequest(
    String senhaAtual,
    String novaSenha
) {
}