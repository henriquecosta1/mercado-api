package com.mercado.domain;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Domínio: Cliente (Entidade Rica)")
class ClienteTest {

    private final TenantId tenantId = TenantId.de(UUID.randomUUID());

    @Test
    @DisplayName("Deve criar cliente com saldo devedor zerado")
    void deveCriarClienteComSaldoZerado() {
        Cliente cliente = Cliente.criar(tenantId, "João da Silva", "11999998888", Dinheiro.de("200.00"));

        assertEquals("João da Silva", cliente.getNome());
        assertEquals("11999998888", cliente.getTelefone());
        assertEquals(new BigDecimal("200.00"), cliente.getLimiteCredito().valor());
        assertEquals(BigDecimal.ZERO.setScale(2), cliente.getSaldoDevedor().valor());
    }

    @Test
    @DisplayName("Deve registrar débito respeitando o limite de crédito")
    void deveRegistrarDebitoComSucesso() {
        Cliente cliente = Cliente.criar(tenantId, "Maria Oliveira", "11988887777", Dinheiro.de("150.00"));
        cliente.registrarDebito(Dinheiro.de("100.00"));

        assertEquals(new BigDecimal("100.00"), cliente.getSaldoDevedor().valor());
    }

    @Test
    @DisplayName("Deve recusar débito quando exceder o limite de crédito")
    void deveRecusarDebitoAcimaDoLimite() {
        Cliente cliente = Cliente.criar(tenantId, "Carlos Souza", "11977776666", Dinheiro.de("50.00"));

        assertThrows(RegraDeNegocioException.class, () -> cliente.registrarDebito(Dinheiro.de("60.00")));
    }

    @Test
    @DisplayName("Deve amortizar débito corretamente")
    void deveAmortizarDebito() {
        Cliente cliente = Cliente.criar(tenantId, "Ana Costa", null, Dinheiro.de("300.00"));
        cliente.registrarDebito(Dinheiro.de("120.00"));
        cliente.amortizarDebito(Dinheiro.de("50.00"));

        assertEquals(new BigDecimal("70.00"), cliente.getSaldoDevedor().valor());
    }

    @Test
    @DisplayName("Deve falhar ao tentar amortizar valor maior que o débito atual")
    void deveFalharAmortizacaoMaiorQueDebito() {
        Cliente cliente = Cliente.criar(tenantId, "Ana Costa", null, Dinheiro.de("300.00"));
        cliente.registrarDebito(Dinheiro.de("30.00"));

        assertThrows(RegraDeNegocioException.class, () -> cliente.amortizarDebito(Dinheiro.de("40.00")));
    }

    @Test
    @DisplayName("Deve permitir exclusão quando cliente possui saldo devedor zerado")
    void devePermitirExclusaoQuandoSaldoZerado() {
        Cliente cliente = Cliente.criar(tenantId, "Pedro Silva", "11911112222", Dinheiro.de("500.00"));

        assertTrue(cliente.podeSerExcluido());
    }

    @Test
    @DisplayName("Não deve permitir exclusão quando cliente possui saldo devedor positivo")
    void naoDevePermitirExclusaoQuandoSaldoPositivo() {
        Cliente cliente = Cliente.criar(tenantId, "Pedro Silva", "11911112222", Dinheiro.de("500.00"));
        cliente.registrarDebito(Dinheiro.de("50.00"));

        assertFalse(cliente.podeSerExcluido());
    }
}
