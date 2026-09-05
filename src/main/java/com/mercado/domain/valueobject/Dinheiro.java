package com.mercado.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa valores monetários.
 * Imutável com precisão de duas casas decimais (scale 2) e arredondamento HALF_EVEN.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "O valor monetário não pode ser nulo.");
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro zero() {
        return ZERO;
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro de(double valor) {
        return new Dinheiro(BigDecimal.valueOf(valor));
    }

    public static Dinheiro de(String valor) {
        Objects.requireNonNull(valor, "A representação em texto do valor monetário não pode ser nula.");
        return new Dinheiro(new BigDecimal(valor.trim()));
    }

    public Dinheiro somar(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor a ser somado não pode ser nulo.");
        return new Dinheiro(this.valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor a ser subtraído não pode ser nulo.");
        return new Dinheiro(this.valor.subtract(outro.valor));
    }

    public boolean isMenorQue(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor para comparação não pode ser nulo.");
        return this.valor.compareTo(outro.valor) < 0;
    }

    public boolean isMaiorQue(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor para comparação não pode ser nulo.");
        return this.valor.compareTo(outro.valor) > 0;
    }

    public boolean isMenorOuIgualA(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor para comparação não pode ser nulo.");
        return this.valor.compareTo(outro.valor) <= 0;
    }

    public boolean isMaiorOuIgualA(Dinheiro outro) {
        Objects.requireNonNull(outro, "O valor para comparação não pode ser nulo.");
        return this.valor.compareTo(outro.valor) >= 0;
    }

    public boolean isZero() {
        return this.valor.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegativo() {
        return this.valor.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isPositivo() {
        return this.valor.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(Dinheiro o) {
        Objects.requireNonNull(o, "O valor para comparação não pode ser nulo.");
        return this.valor.compareTo(o.valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
