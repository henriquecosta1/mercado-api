package com.mercado.api.dto;

public record AlterarPinRequest(
    String pinAtual,
    String novoPin
) {}
