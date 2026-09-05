package com.mercado.domain;

import com.mercado.domain.valueobject.Dinheiro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Domínio: Dinheiro (Value Object)")
class DinheiroTest {

    @Test
    @DisplayName("Deve garantir imutabilidade e escala de 2 casas decimais")
    void deveGarantirDuasCasasDecimais() {
        Dinheiro d = Dinheiro.de(new BigDecimal("10.5"));
        assertEquals(new BigDecimal("10.50"), d.valor());

        Dinheiro d3Casas = Dinheiro.de(new BigDecimal("10.555"));
        assertEquals(new BigDecimal("10.56"), d3Casas.valor());
    }

    @Test
    @DisplayName("Deve somar valores monetários corretamente")
    void deveSomarValores() {
        Dinheiro d1 = Dinheiro.de("10.50");
        Dinheiro d2 = Dinheiro.de("20.25");
        Dinheiro soma = d1.somar(d2);

        assertEquals(new BigDecimal("30.75"), soma.valor());
    }

    @Test
    @DisplayName("Deve subtrair valores monetários corretamente")
    void deveSubtrairValores() {
        Dinheiro d1 = Dinheiro.de("50.00");
        Dinheiro d2 = Dinheiro.de("18.50");
        Dinheiro resultado = d1.subtrair(d2);

        assertEquals(new BigDecimal("31.50"), resultado.valor());
    }

    @Test
    @DisplayName("Deve comparar grandezas monetárias")
    void deveCompararValores() {
        Dinheiro d1 = Dinheiro.de("10.00");
        Dinheiro d2 = Dinheiro.de("20.00");

        assertTrue(d1.isMenorQue(d2));
        assertTrue(d2.isMaiorQue(d1));
        assertFalse(d1.isMaiorQue(d2));
        assertFalse(d2.isMenorQue(d1));
        assertTrue(d1.isMenorOuIgualA(Dinheiro.de("10.00")));
    }
}
