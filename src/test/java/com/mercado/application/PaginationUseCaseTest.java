package com.mercado.application;

import com.mercado.application.dto.PageDTO;
import com.mercado.application.dto.VendaResumoDTO;
import com.mercado.application.usecase.ListarVendasUseCase;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.PageResult;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Aplicação: Server-Side Pagination e ListarVendasUseCase")
class PaginationUseCaseTest {

    private FakeVendaRepository vendaRepository;
    private ListarVendasUseCase listarVendasUseCase;

    private final UUID tenantIdRaw = UUID.randomUUID();
    private final TenantId tenantId = TenantId.de(tenantIdRaw);
    private final UUID caixaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        vendaRepository = new FakeVendaRepository();
        listarVendasUseCase = new ListarVendasUseCase(vendaRepository);
    }

    @Test
    @DisplayName("Deve calcular corretamente os metadados de paginação vazia")
    void deveTratarPaginacaoVazia() {
        PageResult<String> emptyResult = PageResult.empty(0, 10);
        assertEquals(0, emptyResult.content().size());
        assertEquals(0L, emptyResult.totalElements());
        assertEquals(0, emptyResult.totalPages());
        assertTrue(emptyResult.isFirst());
        assertTrue(emptyResult.isLast());

        PageDTO<String> dto = PageDTO.from(emptyResult);
        assertEquals(0, dto.content().size());
        assertEquals(0L, dto.totalElements());
        assertEquals(0, dto.totalPages());
        assertTrue(dto.first());
        assertTrue(dto.last());
    }

    @Test
    @DisplayName("Deve paginar histórico de vendas com filtros e metadados")
    void devePaginarVendasComSucesso() {
        for (int i = 1; i <= 22; i++) {
            Venda v = Venda.criar(
                tenantId,
                caixaId,
                Dinheiro.de("50.00"),
                FormaPagamento.DINHEIRO,
                Dinheiro.zero(),
                "Venda " + i
            );
            vendaRepository.salvar(v);
        }

        // Página 0, tamanho 10
        PageDTO<VendaResumoDTO> p0 = listarVendasUseCase.executar(tenantIdRaw, caixaId, null, null, null, 0, 10);
        assertEquals(10, p0.content().size());
        assertEquals(22L, p0.totalElements());
        assertEquals(3, p0.totalPages());
        assertEquals(0, p0.page());
        assertTrue(p0.first());
        assertFalse(p0.last());

        // Página 2, tamanho 10 (última)
        PageDTO<VendaResumoDTO> p2 = listarVendasUseCase.executar(tenantIdRaw, caixaId, null, null, null, 2, 10);
        assertEquals(2, p2.content().size());
        assertEquals(2, p2.page());
        assertFalse(p2.first());
        assertTrue(p2.last());
    }

    static class FakeVendaRepository implements VendaRepository {
        private final Map<UUID, Venda> store = new LinkedHashMap<>();

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
                .filter(v -> v.getTenantId().equals(tenantId) && caixaId.equals(v.getCaixaId()))
                .toList();
        }

        @Override
        public PageResult<Venda> listarVendasPaginado(TenantId tenantId, UUID caixaId, String status, Instant de, Instant ate, int page, int size) {
            List<Venda> filtradas = store.values().stream()
                .filter(v -> v.getTenantId().equals(tenantId))
                .filter(v -> caixaId == null || caixaId.equals(v.getCaixaId()))
                .filter(v -> status == null || status.equalsIgnoreCase("TODAS") || v.getStatus().name().equalsIgnoreCase(status))
                .toList();

            int fromIndex = Math.min(page * size, filtradas.size());
            int toIndex = Math.min(fromIndex + size, filtradas.size());
            List<Venda> sub = fromIndex <= toIndex ? filtradas.subList(fromIndex, toIndex) : Collections.emptyList();

            return new PageResult<>(sub, page, size, filtradas.size());
        }
    }
}
