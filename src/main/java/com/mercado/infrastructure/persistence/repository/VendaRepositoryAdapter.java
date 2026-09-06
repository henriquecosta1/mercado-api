package com.mercado.infrastructure.persistence.repository;

import com.mercado.application.dto.MetricasFaturamentoDTO;
import com.mercado.application.dto.TopProdutoVendidoDTO;
import com.mercado.application.dto.TotalPorFormaPagamentoDTO;
import com.mercado.application.repository.DashboardRepository;
import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.StatusVenda;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.ItemVendaJpaEntity;
import com.mercado.infrastructure.persistence.entity.VendaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VendaRepositoryAdapter implements VendaRepository, DashboardRepository, PanacheRepositoryBase<VendaJpaEntity, UUID> {

    @Override
    public void salvar(Venda venda) {
        VendaJpaEntity jpaEntity = findById(venda.getId());
        if (jpaEntity != null) {
            jpaEntity.updateFromDomain(venda);
        } else {
            persist(VendaJpaEntity.fromDomain(venda));
            if (venda.getItens() != null && !venda.getItens().isEmpty()) {
                for (ItemVenda item : venda.getItens()) {
                    ItemVendaJpaEntity itemJpa = ItemVendaJpaEntity.fromDomain(item, venda.getId());
                    itemJpa.persist();
                }
            }
        }
    }

    @Override
    public Optional<Venda> buscarPorId(TenantId tenantId, UUID id) {
        return buscarPorIdComItens(id, tenantId);
    }

    @Override
    public Optional<Venda> buscarPorIdComItens(UUID vendaId, TenantId tenantId) {
        Optional<VendaJpaEntity> jpaOptional = find("tenantId = ?1 and id = ?2", tenantId.valor(), vendaId)
            .firstResultOptional();

        if (jpaOptional.isEmpty()) {
            return Optional.empty();
        }

        VendaJpaEntity vendaJpa = jpaOptional.get();
        List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaId).list();
        List<ItemVenda> itens = itensJpa.stream()
            .map(ItemVendaJpaEntity::toDomain)
            .toList();

        return Optional.of(vendaJpa.toDomain(itens));
    }

    @Override
    public List<Venda> listarPorCaixa(UUID caixaId, TenantId tenantId) {
        List<VendaJpaEntity> vendasJpa = find("tenantId = ?1 and caixaId = ?2 order by criadoEm desc", tenantId.valor(), caixaId)
            .list();

        return vendasJpa.stream()
            .map(vendaJpa -> {
                List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaJpa.id).list();
                List<ItemVenda> itens = itensJpa.stream()
                    .map(ItemVendaJpaEntity::toDomain)
                    .toList();
                return vendaJpa.toDomain(itens);
            })
            .toList();
    }

    @Override
    public List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
        return listarPorCaixa(caixaId, tenantId);
    }

    @Override
    public MetricasFaturamentoDTO calcularMetricasFaturamento(TenantId tenantId, Instant de, Instant ate) {
        String jpql = """
            select coalesce(sum(v.valorTotal), 0), count(v.id)
            from VendaJpaEntity v
            where v.tenantId = :tenantId
              and v.status = :status
              and v.criadoEm >= :de
              and v.criadoEm <= :ate
            """;

        Object[] result = (Object[]) getEntityManager().createQuery(jpql)
            .setParameter("tenantId", tenantId.valor())
            .setParameter("status", StatusVenda.CONCLUIDA.name())
            .setParameter("de", de)
            .setParameter("ate", ate)
            .getSingleResult();

        BigDecimal faturamento = result[0] != null
            ? ((BigDecimal) result[0]).setScale(2, RoundingMode.HALF_EVEN)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        int totalVendas = result[1] != null
            ? ((Number) result[1]).intValue()
            : 0;

        BigDecimal ticketMedio = totalVendas > 0
            ? faturamento.divide(BigDecimal.valueOf(totalVendas), 2, RoundingMode.HALF_EVEN)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        return new MetricasFaturamentoDTO(faturamento, totalVendas, ticketMedio);
    }

    @Override
    public List<TotalPorFormaPagamentoDTO> obterDistribuicaoPagamentos(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal) {
        String jpql = """
            select v.formaPagamento, coalesce(sum(v.valorTotal), 0), count(v.id)
            from VendaJpaEntity v
            where v.tenantId = :tenantId
              and v.status = :status
              and v.criadoEm >= :de
              and v.criadoEm <= :ate
            group by v.formaPagamento
            order by sum(v.valorTotal) desc
            """;

        List<Object[]> rows = getEntityManager().createQuery(jpql, Object[].class)
            .setParameter("tenantId", tenantId.valor())
            .setParameter("status", StatusVenda.CONCLUIDA.name())
            .setParameter("de", de)
            .setParameter("ate", ate)
            .getResultList();

        BigDecimal baseCalculo = (faturamentoTotal != null && faturamentoTotal.compareTo(BigDecimal.ZERO) > 0)
            ? faturamentoTotal
            : BigDecimal.ZERO;

        return rows.stream().map(row -> {
            String forma = (String) row[0];
            BigDecimal total = ((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_EVEN);
            int quantidade = ((Number) row[2]).intValue();
            double percentual = baseCalculo.compareTo(BigDecimal.ZERO) > 0
                ? total.multiply(BigDecimal.valueOf(100))
                    .divide(baseCalculo, 2, RoundingMode.HALF_EVEN)
                    .doubleValue()
                : 0.0;

            return new TotalPorFormaPagamentoDTO(forma, total, quantidade, percentual);
        }).toList();
    }

    @Override
    public List<TopProdutoVendidoDTO> obterTopProdutos(TenantId tenantId, Instant de, Instant ate, int limite) {
        String jpql = """
            select i.descricao, coalesce(sum(i.quantidade), 0), coalesce(sum(i.subtotal), 0)
            from ItemVendaJpaEntity i join VendaJpaEntity v on i.vendaId = v.id
            where v.tenantId = :tenantId
              and v.status = :status
              and v.criadoEm >= :de
              and v.criadoEm <= :ate
            group by i.descricao
            order by sum(i.subtotal) desc
            """;

        List<Object[]> rows = getEntityManager().createQuery(jpql, Object[].class)
            .setParameter("tenantId", tenantId.valor())
            .setParameter("status", StatusVenda.CONCLUIDA.name())
            .setParameter("de", de)
            .setParameter("ate", ate)
            .setMaxResults(limite > 0 ? limite : 5)
            .getResultList();

        return rows.stream().map(row -> {
            String nome = (String) row[0];
            BigDecimal qtd = ((BigDecimal) row[1]).setScale(3, RoundingMode.HALF_EVEN);
            BigDecimal subtotal = ((BigDecimal) row[2]).setScale(2, RoundingMode.HALF_EVEN);
            return new TopProdutoVendidoDTO(nome, qtd, subtotal);
        }).toList();
    }
}
