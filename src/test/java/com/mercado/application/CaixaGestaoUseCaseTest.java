package com.mercado.application;

import com.mercado.application.dto.AbrirCaixaInput;
import com.mercado.application.dto.FecharCaixaOutput;
import com.mercado.application.dto.MovimentacaoInput;
import com.mercado.application.dto.ResumoCaixaOutput;
import com.mercado.application.usecase.AbrirCaixaUseCase;
import com.mercado.application.usecase.FecharCaixaUseCase;
import com.mercado.application.usecase.ObterResumoCaixaUseCase;
import com.mercado.application.usecase.RegistrarMovimentacaoUseCase;
import com.mercado.domain.entity.*;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.MovimentacaoCaixaRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: Gestão de Caixa (Abertura, Movimentações, Resumo e Fechamento)")
class CaixaGestaoUseCaseTest {

    private FakeCaixaRepository caixaRepository;
    private FakeVendaRepository vendaRepository;
    private FakeMovimentacaoCaixaRepository movimentacaoRepository;

    private AbrirCaixaUseCase abrirCaixaUseCase;
    private RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;
    private ObterResumoCaixaUseCase obterResumoCaixaUseCase;
    private FecharCaixaUseCase fecharCaixaUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        caixaRepository = new FakeCaixaRepository();
        vendaRepository = new FakeVendaRepository();
        movimentacaoRepository = new FakeMovimentacaoCaixaRepository();

        abrirCaixaUseCase = new AbrirCaixaUseCase(caixaRepository);
        registrarMovimentacaoUseCase = new RegistrarMovimentacaoUseCase(caixaRepository, movimentacaoRepository);
        obterResumoCaixaUseCase = new ObterResumoCaixaUseCase(caixaRepository, vendaRepository, movimentacaoRepository);
        fecharCaixaUseCase = new FecharCaixaUseCase(caixaRepository);
    }

    @Test
    @DisplayName("Deve abrir novo caixa quando não houver outro aberto")
    void deveAbrirNovoCaixa() {
        AbrirCaixaInput input = new AbrirCaixaInput(tenantIdRaw, new BigDecimal("80.00"));
        UUID caixaId = abrirCaixaUseCase.executar(input);

        assertNotNull(caixaId);
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(StatusCaixa.ABERTO, caixa.getStatus());
        assertEquals(new BigDecimal("80.00"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve falhar ao abrir caixa se já existir outro aberto para o tenant")
    void deveFalharAoAbrirCaixaDuplicado() {
        caixaRepository.salvar(Caixa.abrir(tenantId, Dinheiro.de("50.00")));

        AbrirCaixaInput input = new AbrirCaixaInput(tenantIdRaw, new BigDecimal("100.00"));
        assertThrows(RegraDeNegocioException.class, () -> abrirCaixaUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve registrar sangria com sucesso e abater saldo da gaveta")
    void deveRegistrarSangriaComSucesso() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("200.00"));
        caixaRepository.salvar(caixa);

        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SANGRIA",
            new BigDecimal("70.00"),
            "Pagamento de fornecedor de pães"
        );

        UUID movId = registrarMovimentacaoUseCase.executar(input);
        assertNotNull(movId);

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("130.00"), caixaAtualizado.getSaldoDinheiro().valor());
        assertEquals(1, movimentacaoRepository.listarPorCaixa(caixa.getId(), tenantId).size());
    }

    @Test
    @DisplayName("Deve registrar suprimento com sucesso e adicionar saldo à gaveta")
    void deveRegistrarSuprimentoComSucesso() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("50.00"));
        caixaRepository.salvar(caixa);

        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SUPRIMENTO",
            new BigDecimal("100.00"),
            "Troco adicional em moedas"
        );

        UUID movId = registrarMovimentacaoUseCase.executar(input);
        assertNotNull(movId);

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("150.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve falhar sangria quando saldo for insuficiente")
    void deveFalharSangriaSaldoInsuficiente() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("30.00"));
        caixaRepository.salvar(caixa);

        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SANGRIA",
            new BigDecimal("50.00"),
            "Retirada de sócio"
        );

        assertThrows(RegraDeNegocioException.class, () -> registrarMovimentacaoUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve consolidar resumo financeiro do caixa aberto corretamente")
    void deveConsolidarResumoFinanceiro() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixa);

        // Vendas
        vendaRepository.salvar(Venda.criar(tenantId, caixa.getId(), Dinheiro.de("50.00"), FormaPagamento.DINHEIRO, Dinheiro.zero(), "v1"));
        vendaRepository.salvar(Venda.criar(tenantId, caixa.getId(), Dinheiro.de("30.00"), FormaPagamento.PIX, Dinheiro.zero(), "v2"));
        vendaRepository.salvar(Venda.criar(tenantId, caixa.getId(), Dinheiro.de("40.00"), FormaPagamento.CARTAO, Dinheiro.zero(), "v3"));
        vendaRepository.salvar(Venda.criar(tenantId, caixa.getId(), Dinheiro.de("25.00"), FormaPagamento.FIADO, Dinheiro.zero(), "v4"));

        // Movimentações
        movimentacaoRepository.salvar(MovimentacaoCaixa.criar(tenantId, caixa.getId(), TipoMovimentacao.SANGRIA, Dinheiro.de("20.00"), "sangria"));
        movimentacaoRepository.salvar(MovimentacaoCaixa.criar(tenantId, caixa.getId(), TipoMovimentacao.SUPRIMENTO, Dinheiro.de("15.00"), "suprimento"));

        ResumoCaixaOutput resumo = obterResumoCaixaUseCase.executar(tenantIdRaw);

        assertEquals(caixa.getId(), resumo.caixaId());
        assertEquals("ABERTO", resumo.status());
        assertEquals(new BigDecimal("100.00"), resumo.saldoInicial());
        assertEquals(new BigDecimal("50.00"), resumo.totalVendasDinheiro());
        assertEquals(new BigDecimal("30.00"), resumo.totalVendasPix());
        assertEquals(new BigDecimal("40.00"), resumo.totalVendasCartao());
        assertEquals(new BigDecimal("25.00"), resumo.totalVendasFiado());
        assertEquals(new BigDecimal("20.00"), resumo.totalSangrias());
        assertEquals(new BigDecimal("15.00"), resumo.totalSuprimentos());
        assertEquals(4, resumo.quantidadeVendas());
    }

    @Test
    @DisplayName("Deve fechar caixa aberto com sucesso")
    void deveFecharCaixaComSucesso() {
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixa.adicionarDinheiro(Dinheiro.de("50.00"));
        caixaRepository.salvar(caixa);

        FecharCaixaOutput output = fecharCaixaUseCase.executar(tenantIdRaw);

        assertEquals(caixa.getId(), output.caixaId());
        assertEquals(new BigDecimal("150.00"), output.saldoDinheiroFinal());
        assertNotNull(output.fechadoEm());

        Caixa caixaPersistido = caixaRepository.buscarPorId(caixa.getId(), tenantId).orElseThrow();
        assertEquals(StatusCaixa.FECHADO, caixaPersistido.getStatus());
        assertNotNull(caixaPersistido.getFechadoEm());
    }

    // Fakes de teste
    static class FakeCaixaRepository implements CaixaRepository {
        private final Map<UUID, Caixa> store = new HashMap<>();

        @Override
        public Optional<Caixa> buscarCaixaAberto(TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.isAberto())
                .findFirst();
        }

        @Override
        public Optional<Caixa> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void salvar(Caixa caixa) {
            store.put(caixa.getId(), caixa);
        }

        @Override
        public void atualizar(Caixa caixa) {
            store.put(caixa.getId(), caixa);
        }
    }

    static class FakeVendaRepository implements VendaRepository {
        private final Map<UUID, Venda> store = new HashMap<>();

        @Override
        public void salvar(Venda venda) {
            store.put(venda.getId(), venda);
        }

        @Override
        public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
            return store.values().stream()
                .filter(v -> v.getTenantId().equals(tenantId) && v.getCaixaId().equals(caixaId))
                .toList();
        }
    }

    static class FakeMovimentacaoCaixaRepository implements MovimentacaoCaixaRepository {
        private final Map<UUID, MovimentacaoCaixa> store = new HashMap<>();

        @Override
        public void salvar(MovimentacaoCaixa mov) {
            store.put(mov.getId(), mov);
        }

        @Override
        public List<MovimentacaoCaixa> listarPorCaixa(UUID caixaId, TenantId tenantId) {
            return store.values().stream()
                .filter(m -> m.getTenantId().equals(tenantId) && m.getCaixaId().equals(caixaId))
                .toList();
        }
    }
}
