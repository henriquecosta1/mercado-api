package com.mercado.application;

import com.mercado.application.dto.AlterarPinInput;
import com.mercado.application.dto.CancelarVendaInput;
import com.mercado.application.dto.CancelarVendaOutput;
import com.mercado.application.dto.MovimentacaoInput;
import com.mercado.application.dto.ValidarPinInput;
import com.mercado.application.usecase.AlterarPinGerenteUseCase;
import com.mercado.application.usecase.CancelarVendaUseCase;
import com.mercado.application.usecase.RegistrarMovimentacaoUseCase;
import com.mercado.application.usecase.ValidarPinGerenteUseCase;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.entity.StatusVenda;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.PinInvalidoException;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.MovimentacaoCaixaRepository;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.PinGerente;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: Gestão e Permissões por PIN de Gerente")
class SegurancaPinUseCaseTest {

    private FakeTenantRepository tenantRepository;
    private FakeCaixaRepository caixaRepository;
    private FakeVendaRepository vendaRepository;
    private FakeMovimentacaoCaixaRepository movimentacaoCaixaRepository;

    private ValidarPinGerenteUseCase validarPinGerenteUseCase;
    private AlterarPinGerenteUseCase alterarPinGerenteUseCase;
    private RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;
    private CancelarVendaUseCase cancelarVendaUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);
    private Tenant tenant;
    private Caixa caixaAberto;

    @BeforeEach
    void setUp() {
        tenantRepository = new FakeTenantRepository();
        caixaRepository = new FakeCaixaRepository();
        vendaRepository = new FakeVendaRepository();
        movimentacaoCaixaRepository = new FakeMovimentacaoCaixaRepository();

        validarPinGerenteUseCase = new ValidarPinGerenteUseCase(tenantRepository);
        alterarPinGerenteUseCase = new AlterarPinGerenteUseCase(tenantRepository);

        registrarMovimentacaoUseCase = new RegistrarMovimentacaoUseCase(
            caixaRepository,
            movimentacaoCaixaRepository,
            validarPinGerenteUseCase
        );

        cancelarVendaUseCase = new CancelarVendaUseCase(
            vendaRepository,
            caixaRepository,
            new FakeClienteRepositoryStub(),
            new FakeProdutoRepositoryStub(),
            validarPinGerenteUseCase
        );

        // Tenant configurado com PIN inicial padrão "1234"
        tenant = new Tenant(tenantId, "Mercado Central", PinGerente.criar("1234"));
        tenantRepository.salvar(tenant);

        // Caixa aberto com 100.00 de saldo inicial
        caixaAberto = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixaAberto);
    }

    @Test
    @DisplayName("Deve validar PIN correto com sucesso")
    void deveValidarPinCorretoComSucesso() {
        assertDoesNotThrow(() -> validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, "1234")));
    }

    @Test
    @DisplayName("Deve lançar PinInvalidoException ao validar PIN incorreto")
    void deveLancarExcecaoAoValidarPinIncorreto() {
        assertThrows(PinInvalidoException.class, () ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, "9999"))
        );
    }

    @Test
    @DisplayName("Deve lançar PinInvalidoException ao validar PIN nulo ou vazio")
    void deveLancarExcecaoAoValidarPinNuloOuVazio() {
        assertThrows(PinInvalidoException.class, () ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, null))
        );
        assertThrows(PinInvalidoException.class, () ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, ""))
        );
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando tenant não existir")
    void deveLancarExcecaoTenantNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(UUID.randomUUID(), "1234"))
        );
    }

    @Test
    @DisplayName("Deve alterar PIN com sucesso e passar a validar pelo novo PIN")
    void deveAlterarPinComSucesso() {
        alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantIdRaw, "1234", "5678"));

        // PIN antigo agora deve ser rejeitado
        assertThrows(PinInvalidoException.class, () ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, "1234"))
        );

        // Novo PIN deve ser aceito
        assertDoesNotThrow(() ->
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdRaw, "5678"))
        );
    }

    @Test
    @DisplayName("Deve falhar ao alterar PIN informando PIN atual incorreto")
    void deveFalharAoAlterarPinComPinAtualIncorreto() {
        assertThrows(PinInvalidoException.class, () ->
            alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantIdRaw, "0000", "5678"))
        );
    }

    @Test
    @DisplayName("Deve falhar ao alterar PIN informando novo PIN com formato inválido")
    void deveFalharAoAlterarPinComFormatoInvalido() {
        // Menos de 4 dígitos
        assertThrows(RegraDeNegocioException.class, () ->
            alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantIdRaw, "1234", "123"))
        );
        // Mais de 6 dígitos
        assertThrows(RegraDeNegocioException.class, () ->
            alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantIdRaw, "1234", "1234567"))
        );
        // Não numérico
        assertThrows(RegraDeNegocioException.class, () ->
            alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantIdRaw, "1234", "abcd"))
        );
    }

    @Test
    @DisplayName("Deve executar sangria com sucesso quando PIN correto é fornecido")
    void deveExecutarSangriaComPinCorreto() {
        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SANGRIA",
            new BigDecimal("30.00"),
            "Recolhimento cofre",
            "1234"
        );

        UUID movId = registrarMovimentacaoUseCase.executar(input);
        assertNotNull(movId);

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("70.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve bloquear sangria e manter saldo intacto quando PIN incorreto for informado")
    void deveBloquearSangriaComPinIncorreto() {
        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SANGRIA",
            new BigDecimal("30.00"),
            "Recolhimento cofre",
            "9999"
        );

        assertThrows(PinInvalidoException.class, () -> registrarMovimentacaoUseCase.executar(input));

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Não deve exigir PIN para operação de SUPRIMENTO")
    void naoDeveExigirPinParaSuprimento() {
        MovimentacaoInput input = new MovimentacaoInput(
            tenantIdRaw,
            "SUPRIMENTO",
            new BigDecimal("50.00"),
            "Troco inicial extra",
            null // sem PIN
        );

        UUID movId = registrarMovimentacaoUseCase.executar(input);
        assertNotNull(movId);

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("150.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve cancelar venda com sucesso quando PIN do gerente estiver correto")
    void deveCancelarVendaComPinCorreto() {
        caixaAberto.adicionarDinheiro(Dinheiro.de("50.00"));
        caixaRepository.atualizar(caixaAberto);

        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("50.00"),
            FormaPagamento.DINHEIRO,
            Dinheiro.zero(),
            "Venda balcão"
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Engano no troco", "1234");
        CancelarVendaOutput output = cancelarVendaUseCase.executar(input);

        assertNotNull(output);
        assertEquals(StatusVenda.CANCELADA.name(), output.status());

        Venda vendaAtualizada = vendaRepository.buscarPorIdComItens(venda.getId(), tenantId).orElseThrow();
        assertTrue(vendaAtualizada.isCancelada());

        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve bloquear cancelamento de venda quando PIN do gerente estiver incorreto")
    void deveBloquearCancelamentoVendaComPinIncorreto() {
        Venda venda = Venda.criar(
            tenantId,
            caixaAberto.getId(),
            Dinheiro.de("50.00"),
            FormaPagamento.DINHEIRO,
            Dinheiro.zero(),
            "Venda balcão"
        );
        vendaRepository.salvar(venda);

        CancelarVendaInput input = new CancelarVendaInput(tenantIdRaw, venda.getId(), "Engano no troco", "errado");
        assertThrows(PinInvalidoException.class, () -> cancelarVendaUseCase.executar(input));

        Venda vendaAtualizada = vendaRepository.buscarPorIdComItens(venda.getId(), tenantId).orElseThrow();
        assertFalse(vendaAtualizada.isCancelada());
    }

    // Fakes para os testes
    static class FakeTenantRepository implements TenantRepository {
        private final Map<UUID, Tenant> store = new HashMap<>();

        @Override
        public Optional<Tenant> buscarPorId(TenantId id) {
            return Optional.ofNullable(store.get(id.valor()));
        }

        @Override
        public List<Tenant> listarTodos() {
            return List.copyOf(store.values());
        }

        @Override
        public void salvar(Tenant tenant) {
            store.put(tenant.getId().valor(), tenant);
        }

        @Override
        public void atualizar(Tenant tenant) {
            store.put(tenant.getId().valor(), tenant);
        }
    }

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

    static class FakeMovimentacaoCaixaRepository implements MovimentacaoCaixaRepository {
        private final List<MovimentacaoCaixa> store = new ArrayList<>();

        @Override
        public void salvar(MovimentacaoCaixa movimentacao) {
            store.add(movimentacao);
        }

        @Override
        public List<MovimentacaoCaixa> listarPorCaixa(UUID caixaId, TenantId tenantId) {
            return store.stream()
                .filter(m -> m.getCaixaId().equals(caixaId))
                .toList();
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
            Venda v = store.get(id);
            if (v != null && v.getTenantId().equals(tenantId)) {
                return Optional.of(v);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Venda> buscarPorIdComItens(UUID id, TenantId tenantId) {
            return buscarPorId(tenantId, id);
        }
    }

    static class FakeClienteRepositoryStub implements ClienteRepository {
        @Override
        public Optional<com.mercado.domain.entity.Cliente> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<com.mercado.domain.entity.Cliente> buscarPorNomeOuTelefone(TenantId tenantId, String nome, String telefone) {
            return Optional.empty();
        }

        @Override
        public List<com.mercado.domain.entity.Cliente> listarComSaldoDevedor(TenantId tenantId) {
            return List.of();
        }

        @Override
        public List<com.mercado.domain.entity.Cliente> buscarPorNome(String nome, TenantId tenantId) {
            return List.of();
        }

        @Override
        public List<com.mercado.domain.entity.Cliente> listarTodos(TenantId tenantId, String busca, String status, Boolean apenasDevedores) {
            return List.of();
        }

        @Override
        public void excluir(UUID id, TenantId tenantId) {}

        @Override
        public void salvar(com.mercado.domain.entity.Cliente cliente) {}

        @Override
        public void atualizar(com.mercado.domain.entity.Cliente cliente) {}
    }

    static class FakeProdutoRepositoryStub implements ProdutoRepository {
        @Override
        public Optional<com.mercado.domain.entity.Produto> buscarPorId(UUID id, TenantId tenantId) {
            return Optional.empty();
        }

        @Override
        public List<com.mercado.domain.entity.Produto> buscarPorNome(String nome, TenantId tenantId) {
            return List.of();
        }

        @Override
        public List<com.mercado.domain.entity.Produto> listarAtivos(TenantId tenantId) {
            return List.of();
        }

        @Override
        public void salvar(com.mercado.domain.entity.Produto produto) {}
    }
}
