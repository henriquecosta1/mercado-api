package com.mercado.domain.valueobject;

import com.mercado.domain.exception.RegraDeNegocioException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

/**
 * Value Object representando o PIN do gerente com criptografia BCrypt.
 */
public final class PinGerente {

    private final String hash;

    private PinGerente(String hash) {
        this.hash = Objects.requireNonNull(hash, "Hash do PIN não pode ser nulo.");
    }

    public static PinGerente deHash(String hash) {
        return new PinGerente(hash);
    }

    public static PinGerente criar(String rawPin) {
        validarFormato(rawPin);
        String salt = BCrypt.gensalt(12);
        String hashGerado = BCrypt.hashpw(rawPin.trim(), salt);
        return new PinGerente(hashGerado);
    }

    public static void validarFormato(String pin) {
        if (pin == null || !pin.trim().matches("^\\d{4,6}$")) {
            throw new RegraDeNegocioException("O PIN de gerente deve conter entre 4 e 6 dígitos numéricos.");
        }
    }

    public boolean verificar(String pinCandidato) {
        if (pinCandidato == null || pinCandidato.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(pinCandidato.trim(), this.hash);
        } catch (Exception e) {
            return false;
        }
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinGerente that = (PinGerente) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "PinGerente[PROTEGIDO]";
    }
}
