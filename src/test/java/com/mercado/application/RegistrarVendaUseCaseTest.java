package com.mercado.application;

import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.application.usecase.RegistrarVendaUseCase;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: RegistrarVendaUseCase")
class RegistrarVendaUseCaseTest {

    private FakeCaixaRepository caixaRepository;
    private FakeClienteRepository clienteRepository;
    private FakeVendaRepository vendaRepository;
    private RegistrarVendaUseCase useCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        caixaRepository = new FakeCaixaRepository();
        clienteRepository = new FakeClienteRepository();
        vendaRepository = new FakeVendaRepository();
        useCase = new RegistrarVendaUseCase(caixaRepository, clienteRepository, vendaRepository);

        // Abre um caixa para os testes
        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de("100.00"));
        caixaRepository.salvar(caixa);
    }

    @Test
    @DisplayName("Deve registrar venda em DINHEIRO, calcular troco e incrementar gaveta")
    void deveRegistrarVendaEmDinheiro() {
        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("35.00"),
            new BigDecimal("50.00"),
            "DINHEIRO",
            null,
            null,
            "Compra no balcão"
        );

        RegistrarVendaOutput output = useCase.executar(input);

        assertNotNull(output.vendaId());
        assertEquals(new BigDecimal("35.00"), output.valorTotal());
        assertEquals(new BigDecimal("15.00"), output.troco());
        assertNull(output.saldoDevedorCliente());

        // Verifica incremento do saldo da gaveta do caixa (100.00 inicial + 35.00)
        Caixa caixaAtualizado = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("135.00"), caixaAtualizado.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve falhar venda em DINHEIRO quando valor recebido for menor que total")
    void deveFalharVendaDinheiroValorInsuficiente() {
        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("50.00"),
            new BigDecimal("40.00"),
            "DINHEIRO",
            null,
            null,
            "Compra"
        );

        assertThrows(RegraDeNegocioException.class, () -> useCase.executar(input));
    }

    @Test
    @DisplayName("Deve registrar venda FIADO para cliente novo criando o registro com saldo devedor")
    void deveRegistrarVendaFiadoClienteNovo() {
        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("42.50"),
            null,
            "FIADO",
            "Seu Zé do Bar",
            "11999990000",
            "Conta semanal"
        );

        RegistrarVendaOutput output = useCase.executar(input);

        assertNotNull(output.vendaId());
        assertEquals(new BigDecimal("42.50"), output.valorTotal());
        assertEquals(BigDecimal.ZERO.setScale(2), output.troco());
        assertEquals(new BigDecimal("42.50"), output.saldoDevedorCliente());

        // Gaveta não deve ter sido alterada
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixa.getSaldoDinheiro().valor());
    }

    @Test
    @DisplayName("Deve registrar venda em PIX sem alterar saldo da gaveta")
    void deveRegistrarVendaEmPix() {
        RegistrarVendaInput input = new RegistrarVendaInput(
            tenantIdRaw,
            new BigDecimal("80.00"),
            null,
            "PIX",
            null,
            null,
            "Pagamento QR Code"
        );

        RegistrarVendaOutput output = useCase.executar(input);

        assertNotNull(output.vendaId());
        assertEquals(new BigDecimal("80.00"), output.valorTotal());
        assertEquals(BigDecimal.ZERO.setScale(2), output.troco());
        assertNull(output.saldoDevedorCliente());

        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId).orElseThrow();
        assertEquals(new BigDecimal("100.00"), caixa.getSaldoDinheiro().valor());
    }

    // Fakes de Repositório em memória para desacoplar completamente os testes do banco de dados
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
        public void salvar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
        }

        @Override
        public void atualizar(Cliente cliente) {
            store.put(cliente.getId(), cliente);
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
}
