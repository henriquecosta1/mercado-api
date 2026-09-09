package com.mercado.domain.exception;

/**
 * Exceção lançada quando o acesso de um estabelecimento está bloqueado
 * por pendência de aprovação (TENANT_PENDENTE) ou licença/mensalidade vencida (TENANT_VENCIDO).
 */
public class AcessoTenantBloqueadoException extends DomainException {

    private final String codigo;

    public AcessoTenantBloqueadoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensagem() {
        return getMessage();
    }
}
