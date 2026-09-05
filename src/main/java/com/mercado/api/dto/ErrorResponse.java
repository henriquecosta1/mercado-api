package com.mercado.api.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    String erro,
    String mensagem,
    int status,
    LocalDateTime timestamp
) {
    public static ErrorResponse of(String erro, String mensagem, int status) {
        return new ErrorResponse(erro, mensagem, status, LocalDateTime.now());
    }
}
