package com.mercado.domain;

import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.StatusCaixa;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Domínio: Caixa (Entidade Rica)")
class CaixaTest {

    private final TenantId tenantId = TenantId.de(UUID.randomUUID());

    @Test
    @DisplayName("Deve abrir caixa com saldo inicial correto")
    void deveAbrirCaixaCorretamente() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));

        assertEquals(StatusCaixa.ABERTO, caixa.getStatus());
        assertEquals(new BigDecimal("100.00"), caixa.getSaldoInicial().valor());
        assertEquals(new BigDecimal("100.00"), caixa.getSaldoDinheiro().valor());
        assertNotNull(caixa.getAbertoEm());
        assertNull(caixa.getFechadoEm());
    }

    @Test
    @DisplayName("Deve adicionar dinheiro ao saldo do caixa aberto")
    void deveAdicionarDinheiro() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("50.00"));
        caixa.adicionarDinheiro(Dinheiro.de("35.50"));

        assertEquals(new BigDecimal("85.50"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve realizar sangria com sucesso quando houver saldo")
    void deveRealizarSangria() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixa.sangria(Dinheiro.de("40.00"));

        assertEquals(new BigDecimal("60.00"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve falhar sangria quando saldo for insuficiente")
    void deveFalharSangriaSaldoInsuficiente() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("30.00"));

        assertThrows(RegraDeNegocioException.class, () -> caixa.sangria(Dinheiro.de("50.00")));
    }

    @Test
    @DisplayName("Deve fechar caixa e não permitir mais movimentações")
    void deveFecharCaixaENaoPermitirMovimentacoes() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("50.00"));
        caixa.fechar();

        assertEquals(StatusCaixa.FECHADO, caixa.getStatus());
        assertNotNull(caixa.getFechadoEm());

        assertThrows(RegraDeNegocioException.class, () -> caixa.adicionarDinheiro(Dinheiro.de("10.00")));
        assertThrows(RegraDeNegocioException.class, () -> caixa.sangria(Dinheiro.de("10.00")));
    }
}
