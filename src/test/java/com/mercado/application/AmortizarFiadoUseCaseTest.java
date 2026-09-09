package com.mercado.application;

import com.mercado.application.dto.AmortizarFiadoInput;
import com.mercado.application.dto.AmortizarFiadoOutput;
import com.mercado.application.dto.ClienteDTO;
import com.mercado.application.usecase.AmortizarFiadoUseCase;
import com.mercado.application.usecase.ListarClientesFiadoUseCase;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: AmortizarFiadoUseCase e ListarClientesFiadoUseCase")
class AmortizarFiadoUseCaseTest {

    private FakeCaixaRepository caixaRepository;
    private FakeClienteRepository clienteRepository;
    private AmortizarFiadoUseCase amortizarFiadoUseCase;
    private ListarClientesFiadoUseCase listarClientesFiadoUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        caixaRepository = new FakeCaixaRepository();
        clienteRepository = new FakeClienteRepository();
        amortizarFiadoUseCase = new AmortizarFiadoUseCase(clienteRepository, caixaRepository);
        listarClientesFiadoUseCase = new ListarClientesFiadoUseCase(clienteRepository);

        // Abre um caixa para o tenant
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("50.00"));
        caixaRepository.salvar(caixa);
    }

    @Test
    @DisplayName("Deve amortizar dívida em DINHEIRO, abater o saldo devedor e incrementar a gaveta do caixa")
    void deveAmortizarDividaEmDinheiro() {
        Cliente cliente = Cliente.criar(tenantId, "Carlos Eduardo", "11911112222", Dinheiro.de("500.00"));
        cliente.registrarDebito(Dinheiro.de("200.00"));
        clienteRepository.salvar(cliente);

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            cliente.getId(),
            new BigDecimal("80.00"),
            "DINHEIRO"
        );

        AmortizarFiadoOutput output = amortizarFiadoUseCase.executar(input);

        assertEquals(cliente.getId(), output.clienteId());
        assertEquals(new BigDecimal("80.00"), output.valorPago());
        assertEquals(new BigDecimal("120.00"), output.novoSaldoDevedor());

        // Verifica gaveta do caixa (50.00 + 80.00 = 130.00)
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("130.00"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve amortizar dívida via PIX sem alterar saldo da gaveta do caixa")
    void deveAmortizarDividaEmPix() {
        Cliente cliente = Cliente.criar(tenantId, "Mariana Rios", "11933334444", Dinheiro.de("300.00"));
        cliente.registrarDebito(Dinheiro.de("150.00"));
        clienteRepository.salvar(cliente);

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            cliente.getId(),
            new BigDecimal("150.00"),
            "PIX"
        );

        AmortizarFiadoOutput output = amortizarFiadoUseCase.executar(input);

        assertEquals(cliente.getId(), output.clienteId());
        assertEquals(new BigDecimal("150.00"), output.valorPago());
        assertEquals(BigDecimal.ZERO.setScale(2), output.novoSaldoDevedor());

        // Gaveta permanece intacta
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("50.00"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve falhar ao tentar amortizar valor maior que o saldo devedor do cliente")
    void deveFalharAmortizacaoSuperiorAoSaldoDevedor() {
        Cliente cliente = Cliente.criar(tenantId, "Joana Prado", null, Dinheiro.de("200.00"));
        cliente.registrarDebito(Dinheiro.de("50.00"));
        clienteRepository.salvar(cliente);

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            cliente.getId(),
            new BigDecimal("60.00"),
            "DINHEIRO"
        );

        assertThrows(RegraDeNegocioException.class, () -> amortizarFiadoUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve falhar ao tentar amortizar cliente inexistente")
    void deveFalharClienteInexistente() {
        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            UUID.randomUUID(),
            new BigDecimal("50.00"),
            "DINHEIRO"
        );

        assertThrows(RecursoNaoEncontradoException.class, () -> amortizarFiadoUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve falhar quando não houver caixa aberto para o tenant")
    void deveFalharSemCaixaAberto() {
        Cliente cliente = Cliente.criar(tenantId, "Paula Fernandes", null, Dinheiro.de("200.00"));
        cliente.registrarDebito(Dinheiro.de("100.00"));
        clienteRepository.salvar(cliente);

        // Fecha o caixa
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        caixa.fechar();
        caixaRepository.atualizar(caixa);

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantIdRaw,
            cliente.getId(),
            new BigDecimal("50.00"),
            "DINHEIRO"
        );

        assertThrows(RegraDeNegocioException.class, () -> amortizarFiadoUseCase.executar(input));
    }

    @Test
    @DisplayName("Deve listar apenas clientes com saldo devedor maior que zero")
    void deveListarApenasClientesComSaldoDevedor() {
        Cliente c1 = Cliente.criar(tenantId, "Devedor 1", "111", Dinheiro.de("500.00"));
        c1.registrarDebito(Dinheiro.de("100.00"));
        clienteRepository.salvar(c1);

        Cliente c2 = Cliente.criar(tenantId, "Em Dia", "222", Dinheiro.de("500.00"));
        clienteRepository.salvar(c2);

        List<ClienteDTO> list = listarClientesFiadoUseCase.executar(tenantIdRaw, null);

        assertEquals(1, list.size());
        assertEquals("Devedor 1", list.get(0).nome());
    }

    @Test
    @DisplayName("Deve filtrar clientes por busca de nome")
    void deveFiltrarPorNome() {
        Cliente c1 = Cliente.criar(tenantId, "Roberto Carlos", "111", Dinheiro.de("500.00"));
        c1.registrarDebito(Dinheiro.de("100.00"));
        clienteRepository.salvar(c1);

        Cliente c2 = Cliente.criar(tenantId, "Erasmo Carlos", "222", Dinheiro.de("500.00"));
        c2.registrarDebito(Dinheiro.de("50.00"));
        clienteRepository.salvar(c2);

        List<ClienteDTO> list = listarClientesFiadoUseCase.executar(tenantIdRaw, "Erasmo");

        assertEquals(1, list.size());
        assertEquals("Erasmo Carlos", list.get(0).nome());
    }

    // Fakes para testes
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

    static class FakeClienteRepository implements ClienteRepository {
        private final Map<UUID, Cliente> store = new HashMap<>();

        @Override
        public Optional<Cliente> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Cliente> buscarPorNomeOuTelefone(TenantId tenantId, String nome, String telefone) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId))
                .filter(c -> (telefone != null && telefone.equals(c.getTelefone())) ||
                             c.getNome().equalsIgnoreCase(nome))
                .findFirst();
        }

        @Override
        public List<Cliente> listarComSaldoDevedor(TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.getSaldoDevedor().isMaiorQue(Dinheiro.zero()))
                .toList();
        }

        @Override
        public List<Cliente> buscarPorNome(String nome, TenantId tenantId) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId) && c.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
        }

        @Override
        public List<Cliente> listarTodos(TenantId tenantId, String busca, String status, Boolean apenasDevedores) {
            return store.values().stream()
                .filter(c -> c.getTenantId().equals(tenantId))
                .filter(c -> {
                    if (busca == null || busca.isBlank()) return true;
                    String b = busca.toLowerCase().trim();
                    return (c.getNome() != null && c.getNome().toLowerCase().contains(b))
                        || (c.getApelido() != null && c.getApelido().toLowerCase().contains(b))
                        || (c.getTelefone() != null && c.getTelefone().contains(b))
                        || (c.getCpf() != null && c.getCpf().contains(b));
                })
                .filter(c -> {
                    if (status == null || status.isBlank() || status.equalsIgnoreCase("TODOS")) return true;
                    return c.getStatus().name().equalsIgnoreCase(status.trim());
                })
                .filter(c -> {
                    if (Boolean.TRUE.equals(apenasDevedores)) {
                        return c.getSaldoDevedor().isMaiorQue(Dinheiro.zero());
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        }

        @Override
        public void excluir(UUID id, TenantId tenantId) {
            store.remove(id);
        }

        @Override
        public void salvar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
        }

        @Override
        public void atualizar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
        }
    }
}
