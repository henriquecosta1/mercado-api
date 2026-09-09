package com.mercado.application;

import com.mercado.application.dto.ClienteDTO;
import com.mercado.application.dto.ValidarPinInput;
import com.mercado.application.usecase.ExcluirClienteUseCase;
import com.mercado.application.usecase.ListarClientesUseCase;
import com.mercado.application.usecase.ValidarPinGerenteUseCase;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.StatusCliente;
import com.mercado.domain.exception.ClienteComDebitoException;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: ListarClientesUseCase e ExcluirClienteUseCase")
class ClienteGestaoUseCaseTest {

    private FakeClienteRepository clienteRepository;
    private ListarClientesUseCase listarClientesUseCase;
    private ExcluirClienteUseCase excluirClienteUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);

    @BeforeEach
    void setUp() {
        clienteRepository = new FakeClienteRepository();
        listarClientesUseCase = new ListarClientesUseCase(clienteRepository);
        excluirClienteUseCase = new ExcluirClienteUseCase(clienteRepository);
    }

    @Test
    @DisplayName("Deve listar todos os clientes (incluindo saldo zerado) para PDV/Gestão quando apenasDevedores for false ou null")
    void deveListarTodosOsClientesSemFiltroDeDevedores() {
        Cliente c1 = Cliente.criar(tenantId, "Bruna Silva", "11911111111", Dinheiro.de("500.00")); // saldo 0
        Cliente c2 = Cliente.criar(tenantId, "Carlos Alberto", "11922222222", Dinheiro.de("300.00"));
        c2.registrarDebito(Dinheiro.de("80.00")); // saldo 80

        clienteRepository.salvar(c1);
        clienteRepository.salvar(c2);

        List<ClienteDTO> resultado = listarClientesUseCase.executar(tenantIdRaw, null, null, false);

        assertEquals(2, resultado.size());
        assertEquals("Bruna Silva", resultado.get(0).nome());
        assertEquals(BigDecimal.ZERO.setScale(2), resultado.get(0).saldoDevedor());
        assertEquals("Carlos Alberto", resultado.get(1).nome());
        assertEquals(new BigDecimal("80.00"), resultado.get(1).saldoDevedor());
    }

    @Test
    @DisplayName("Deve listar apenas clientes devedores para o Caderninho quando apenasDevedores for true")
    void deveListarApenasDevedoresQuandoApenasDevedoresForTrue() {
        Cliente c1 = Cliente.criar(tenantId, "Bruna Silva", "11911111111", Dinheiro.de("500.00")); // saldo 0
        Cliente c2 = Cliente.criar(tenantId, "Carlos Alberto", "11922222222", Dinheiro.de("300.00"));
        c2.registrarDebito(Dinheiro.de("80.00")); // saldo 80

        clienteRepository.salvar(c1);
        clienteRepository.salvar(c2);

        List<ClienteDTO> resultado = listarClientesUseCase.executar(tenantIdRaw, null, null, true);

        assertEquals(1, resultado.size());
        assertEquals("Carlos Alberto", resultado.get(0).nome());
        assertEquals(new BigDecimal("80.00"), resultado.get(0).saldoDevedor());
    }

    @Test
    @DisplayName("Deve filtrar clientes por busca de nome, apelido, telefone ou CPF")
    void deveFiltrarClientesPorBusca() {
        Cliente c1 = Cliente.criar(tenantId, "Marcos Andrade", "Marcão", "11977778888", "12345678901", null, null, Dinheiro.de("1000.00"), 10, null);
        Cliente c2 = Cliente.criar(tenantId, "Juliana Paes", null, "11955554444", "98765432100", null, null, Dinheiro.de("500.00"), 10, null);

        clienteRepository.salvar(c1);
        clienteRepository.salvar(c2);

        // Busca por apelido
        List<ClienteDTO> resApelido = listarClientesUseCase.executar(tenantIdRaw, "marcão", null, false);
        assertEquals(1, resApelido.size());
        assertEquals("Marcos Andrade", resApelido.get(0).nome());

        // Busca por CPF
        List<ClienteDTO> resCpf = listarClientesUseCase.executar(tenantIdRaw, "987654", null, false);
        assertEquals(1, resCpf.size());
        assertEquals("Juliana Paes", resCpf.get(0).nome());
    }

    @Test
    @DisplayName("Deve filtrar clientes por status ATIVO ou BLOQUEADO")
    void deveFiltrarClientesPorStatus() {
        Cliente c1 = Cliente.criar(tenantId, "Cliente Ativo", "11911110000", Dinheiro.de("200.00"));
        Cliente c2 = Cliente.criar(tenantId, "Cliente Bloqueado", "11922220000", Dinheiro.de("200.00"));
        c2.bloquear("Inadimplência prolongada");

        clienteRepository.salvar(c1);
        clienteRepository.salvar(c2);

        List<ClienteDTO> ativos = listarClientesUseCase.executar(tenantIdRaw, null, "ATIVO", false);
        assertEquals(1, ativos.size());
        assertEquals("Cliente Ativo", ativos.get(0).nome());

        List<ClienteDTO> bloqueados = listarClientesUseCase.executar(tenantIdRaw, null, "BLOQUEADO", false);
        assertEquals(1, bloqueados.size());
        assertEquals("Cliente Bloqueado", bloqueados.get(0).nome());
        assertEquals("BLOQUEADO", bloqueados.get(0).status());
        assertEquals("Inadimplência prolongada", bloqueados.get(0).motivoBloqueio());
        assertEquals(10, bloqueados.get(0).diaVencimento());
    }

    @Test
    @DisplayName("Deve excluir cliente com sucesso quando saldoDevedor for zero")
    void deveExcluirClienteComSucessoQuandoSaldoZerado() {
        Cliente c1 = Cliente.criar(tenantId, "Cliente Sem Debito", "11900001111", Dinheiro.de("200.00"));
        clienteRepository.salvar(c1);

        assertDoesNotThrow(() -> excluirClienteUseCase.executar(tenantIdRaw, c1.getId()));
        assertTrue(clienteRepository.buscarPorId(tenantId, c1.getId()).isEmpty());
    }

    @Test
    @DisplayName("Deve lançar ClienteComDebitoException ao tentar excluir cliente com saldo devedor positivo")
    void deveLancarExcecaoAoExcluirClienteComDebito() {
        Cliente c1 = Cliente.criar(tenantId, "Cliente Devedor", "11900002222", Dinheiro.de("200.00"));
        c1.registrarDebito(Dinheiro.de("45.00"));
        clienteRepository.salvar(c1);

        assertThrows(ClienteComDebitoException.class, () -> excluirClienteUseCase.executar(tenantIdRaw, c1.getId()));
        assertTrue(clienteRepository.buscarPorId(tenantId, c1.getId()).isPresent());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar excluir cliente inexistente")
    void deveLancarExcecaoAoExcluirClienteInexistente() {
        UUID idInexistente = UUID.randomUUID();
        assertThrows(RecursoNaoEncontradoException.class,
            () -> excluirClienteUseCase.executar(tenantIdRaw, idInexistente));
    }

    @Test
    @DisplayName("Deve paginar clientes corretamente com cálculo de metadados")
    void devePaginarClientesCorretamente() {
        for (int i = 1; i <= 15; i++) {
            Cliente c = Cliente.criar(tenantId, String.format("Cliente %02d", i), "119999900" + (i < 10 ? "0" + i : i), Dinheiro.de("100.00"));
            clienteRepository.salvar(c);
        }

        // Página 0, tamanho 5
        var p0 = listarClientesUseCase.executarPaginado(tenantIdRaw, null, null, false, 0, 5);
        assertEquals(5, p0.content().size());
        assertEquals(15L, p0.totalElements());
        assertEquals(3, p0.totalPages());
        assertEquals(0, p0.page());
        assertTrue(p0.first());
        assertFalse(p0.last());
        assertEquals("Cliente 01", p0.content().get(0).nome());

        // Página 2, tamanho 5 (última página)
        var p2 = listarClientesUseCase.executarPaginado(tenantIdRaw, null, null, false, 2, 5);
        assertEquals(5, p2.content().size());
        assertEquals(2, p2.page());
        assertFalse(p2.first());
        assertTrue(p2.last());
        assertEquals("Cliente 11", p2.content().get(0).nome());
    }

    static class FakeClienteRepository implements ClienteRepository {
        private final Map<UUID, Cliente> store = new HashMap<>();

        @Override
        public Optional<Cliente> buscarPorId(TenantId tenantId, UUID id) {
            return Optional.ofNullable(store.get(id))
                .filter(c -> c.getTenantId().equals(tenantId));
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
