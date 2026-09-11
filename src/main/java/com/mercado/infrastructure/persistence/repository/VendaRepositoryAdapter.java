package com.mercado.infrastructure.persistence.repository;

import com.mercado.application.dto.DivisaoPagamentoDTO;
import com.mercado.application.dto.LucroBrutoDTO;
import com.mercado.application.dto.MetricasFaturamentoDTO;
import com.mercado.application.dto.TopProdutoVendidoDTO;
import com.mercado.application.dto.TotalPorFormaPagamentoDTO;
import com.mercado.application.dto.TurnoDetalheDTO;
import com.mercado.application.dto.TurnosVendaDTO;
import com.mercado.application.repository.DashboardRepository;
import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.StatusVenda;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.persistence.entity.AmortizacaoJpaEntity;
import com.mercado.infrastructure.persistence.entity.ItemVendaJpaEntity;
import com.mercado.infrastructure.persistence.entity.VendaJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
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
    public com.mercado.domain.repository.PageResult<Venda> listarPorCaixaPaginado(TenantId tenantId, UUID caixaId, int page, int size) {
        var panacheQuery = find("tenantId = ?1 and caixaId = ?2 order by criadoEm desc", tenantId.valor(), caixaId);
        long totalElements = panacheQuery.count();

        List<VendaJpaEntity> vendasJpa = panacheQuery.page(io.quarkus.panache.common.Page.of(page, size)).list();

        List<Venda> content = vendasJpa.stream()
            .map(vendaJpa -> {
                List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaJpa.id).list();
                List<ItemVenda> itens = itensJpa.stream()
                    .map(ItemVendaJpaEntity::toDomain)
                    .toList();
                return vendaJpa.toDomain(itens);
            })
            .toList();

        return new com.mercado.domain.repository.PageResult<>(content, page, size, totalElements);
    }

    @Override
    public com.mercado.domain.repository.PageResult<Venda> listarVendasPaginado(TenantId tenantId, UUID caixaId, String status, Instant de, Instant ate, int page, int size) {
        StringBuilder query = new StringBuilder("tenantId = :tenantId");
        io.quarkus.panache.common.Parameters params = io.quarkus.panache.common.Parameters.with("tenantId", tenantId.valor());

        if (caixaId != null) {
            query.append(" and caixaId = :caixaId");
            params.and("caixaId", caixaId);
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("TODAS")) {
            query.append(" and status = :status");
            params.and("status", status.trim().toUpperCase());
        }
        if (de != null) {
            query.append(" and criadoEm >= :de");
            params.and("de", de);
        }
        if (ate != null) {
            query.append(" and criadoEm <= :ate");
            params.and("ate", ate);
        }

        query.append(" order by criadoEm desc");

        var panacheQuery = find(query.toString(), params);
        long totalElements = panacheQuery.count();

        List<VendaJpaEntity> vendasJpa = panacheQuery.page(io.quarkus.panache.common.Page.of(page, size)).list();

        List<Venda> content = vendasJpa.stream()
            .map(vendaJpa -> {
                List<ItemVendaJpaEntity> itensJpa = ItemVendaJpaEntity.find("vendaId = ?1", vendaJpa.id).list();
                List<ItemVenda> itens = itensJpa.stream()
                    .map(ItemVendaJpaEntity::toDomain)
                    .toList();
                return vendaJpa.toDomain(itens);
            })
            .toList();

        return new com.mercado.domain.repository.PageResult<>(content, page, size, totalElements);
    }

    @Override
    public List<Venda> listarFiadoPorCliente(UUID clienteId, TenantId tenantId) {
        List<VendaJpaEntity> vendasJpa = find(
            "tenantId = ?1 and clienteId = ?2 and formaPagamento = 'FIADO' and status = 'CONCLUIDA' order by criadoEm desc",
            tenantId.valor(), clienteId
        ).list();

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

    @Override
    public LucroBrutoDTO calcularLucroBruto(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal) {
        String jpql = """
            select 
                coalesce(sum((i.precoUnitario - coalesce(p.precoCusto, 0)) * i.quantidade), 0),
                coalesce(sum(case when p.precoCusto is not null then (i.precoUnitario - p.precoCusto) * i.quantidade else 0 end), 0),
                coalesce(sum(case when p.precoCusto is not null then i.subtotal else 0 end), 0)
            from ItemVendaJpaEntity i
            join VendaJpaEntity v on i.vendaId = v.id
            left join ProdutoJpaEntity p on i.produtoId = p.id
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

        BigDecimal lucroBruto = toBigDecimal(result[0]);
        BigDecimal faturamentoComCustoDefinido = toBigDecimal(result[2]);

        BigDecimal baseCalculo = (faturamentoTotal != null && faturamentoTotal.compareTo(BigDecimal.ZERO) > 0)
            ? faturamentoTotal
            : (faturamentoComCustoDefinido.compareTo(BigDecimal.ZERO) > 0 ? faturamentoComCustoDefinido : BigDecimal.ZERO);

        double margemPercentual = baseCalculo.compareTo(BigDecimal.ZERO) > 0
            ? lucroBruto.multiply(BigDecimal.valueOf(100))
                .divide(baseCalculo, 2, RoundingMode.HALF_EVEN)
                .doubleValue()
            : 0.0;

        return new LucroBrutoDTO(lucroBruto, margemPercentual, faturamentoComCustoDefinido);
    }

    @Override
    public DivisaoPagamentoDTO calcularDivisaoPagamento(TenantId tenantId, Instant de, Instant ate, BigDecimal faturamentoTotal) {
        String jpql = """
            select 
                coalesce(sum(case when v.formaPagamento != 'FIADO' then v.valorTotal else 0 end), 0),
                coalesce(sum(case when v.formaPagamento = 'FIADO' then v.valorTotal else 0 end), 0),
                count(case when v.formaPagamento != 'FIADO' then 1 else null end),
                count(case when v.formaPagamento = 'FIADO' then 1 else null end)
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

        BigDecimal vendasAVista = toBigDecimal(result[0]);
        BigDecimal vendasFiado = toBigDecimal(result[1]);
        int totalVendasAVista = toInt(result[2]);
        int totalVendasFiado = toInt(result[3]);

        // Busca amortizações de fiado (pagamentos recebidos) no mesmo período
        String jpqlAmortizacoes = """
            select coalesce(sum(a.valor), 0), count(a.id)
            from AmortizacaoJpaEntity a
            where a.tenantId = :tenantId
              and a.criadoEm >= :de
              and a.criadoEm <= :ate
            """;

        Object[] resultAmort = (Object[]) getEntityManager().createQuery(jpqlAmortizacoes)
            .setParameter("tenantId", tenantId.valor())
            .setParameter("de", de)
            .setParameter("ate", ate)
            .getSingleResult();

        BigDecimal totalAmortizado = toBigDecimal(resultAmort[0]);

        // Total recebido real: vendas à vista + amortizações recebidas
        BigDecimal totalRecebido = vendasAVista.add(totalAmortizado);

        // Fiado que ainda está em aberto/em haver referente ao período (abate as amortizações)
        BigDecimal totalEmHaver = vendasFiado.subtract(totalAmortizado);
        if (totalEmHaver.compareTo(BigDecimal.ZERO) < 0) {
            totalEmHaver = BigDecimal.ZERO;
        }

        BigDecimal baseCalculo = totalRecebido.add(totalEmHaver);
        if (faturamentoTotal != null && faturamentoTotal.compareTo(baseCalculo) > 0) {
            baseCalculo = faturamentoTotal;
        }

        double percentualFiado = baseCalculo.compareTo(BigDecimal.ZERO) > 0
            ? totalEmHaver.multiply(BigDecimal.valueOf(100))
                .divide(baseCalculo, 2, RoundingMode.HALF_EVEN)
                .doubleValue()
            : 0.0;

        double percentualAVista = baseCalculo.compareTo(BigDecimal.ZERO) > 0
            ? totalRecebido.multiply(BigDecimal.valueOf(100))
                .divide(baseCalculo, 2, RoundingMode.HALF_EVEN)
                .doubleValue()
            : 0.0;

        return new DivisaoPagamentoDTO(
            totalRecebido,
            totalEmHaver,
            percentualFiado,
            percentualAVista,
            totalVendasAVista,
            totalVendasFiado,
            totalAmortizado
        );
    }

    @Override
    public TurnosVendaDTO calcularTurnosVenda(TenantId tenantId, Instant de, Instant ate, ZoneId zoneId) {
        String tz = (zoneId != null) ? zoneId.getId() : "America/Sao_Paulo";

        String sql = """
            SELECT 
              COALESCE(SUM(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 6 AND 11 THEN v.valor_total ELSE 0 END), 0) AS total_manha,
              COUNT(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 6 AND 11 THEN 1 END) AS count_manha,
              
              COALESCE(SUM(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 12 AND 17 THEN v.valor_total ELSE 0 END), 0) AS total_tarde,
              COUNT(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 12 AND 17 THEN 1 END) AS count_tarde,
              
              COALESCE(SUM(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 18 AND 23 THEN v.valor_total ELSE 0 END), 0) AS total_noite,
              COUNT(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 18 AND 23 THEN 1 END) AS count_noite,
              
              COALESCE(SUM(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 0 AND 5 THEN v.valor_total ELSE 0 END), 0) AS total_madrugada,
              COUNT(CASE WHEN EXTRACT(HOUR FROM (v.criado_em AT TIME ZONE 'UTC' AT TIME ZONE :tz)) BETWEEN 0 AND 5 THEN 1 END) AS count_madrugada
            FROM vendas v
            WHERE v.tenant_id = :tenantId
              AND v.status = :status
              AND v.criado_em >= :de
              AND v.criado_em <= :ate
            """;

        Object[] row = (Object[]) getEntityManager().createNativeQuery(sql)
            .setParameter("tenantId", tenantId.valor())
            .setParameter("status", StatusVenda.CONCLUIDA.name())
            .setParameter("de", de)
            .setParameter("ate", ate)
            .setParameter("tz", tz)
            .getSingleResult();

        BigDecimal totalManha = toBigDecimal(row[0]);
        int countManha = toInt(row[1]);

        BigDecimal totalTarde = toBigDecimal(row[2]);
        int countTarde = toInt(row[3]);

        BigDecimal totalNoite = toBigDecimal(row[4]);
        int countNoite = toInt(row[5]);

        BigDecimal totalMadrugada = toBigDecimal(row[6]);
        int countMadrugada = toInt(row[7]);

        TurnoDetalheDTO manha = new TurnoDetalheDTO(totalManha, countManha);
        TurnoDetalheDTO tarde = new TurnoDetalheDTO(totalTarde, countTarde);
        TurnoDetalheDTO noite = new TurnoDetalheDTO(totalNoite, countNoite);
        TurnoDetalheDTO madrugada = new TurnoDetalheDTO(totalMadrugada, countMadrugada);

        String turnoMaiorMovimento = determinarTurnoMaiorMovimento(manha, tarde, noite, madrugada);
        return new TurnosVendaDTO(manha, tarde, noite, madrugada, turnoMaiorMovimento);
    }

    private String determinarTurnoMaiorMovimento(TurnoDetalheDTO manha, TurnoDetalheDTO tarde, TurnoDetalheDTO noite, TurnoDetalheDTO madrugada) {
        if (manha.faturamento().compareTo(BigDecimal.ZERO) == 0 &&
            tarde.faturamento().compareTo(BigDecimal.ZERO) == 0 &&
            noite.faturamento().compareTo(BigDecimal.ZERO) == 0 &&
            madrugada.faturamento().compareTo(BigDecimal.ZERO) == 0 &&
            manha.totalVendas() == 0 && tarde.totalVendas() == 0 && noite.totalVendas() == 0 && madrugada.totalVendas() == 0) {
            return "-";
        }

        String maior = "ManhÃ£";
        BigDecimal maiorFaturamento = manha.faturamento();
        int maiorVendas = manha.totalVendas();

        if (tarde.faturamento().compareTo(maiorFaturamento) > 0 ||
            (tarde.faturamento().compareTo(maiorFaturamento) == 0 && tarde.totalVendas() > maiorVendas)) {
            maior = "Tarde";
            maiorFaturamento = tarde.faturamento();
            maiorVendas = tarde.totalVendas();
        }

        if (noite.faturamento().compareTo(maiorFaturamento) > 0 ||
            (noite.faturamento().compareTo(maiorFaturamento) == 0 && noite.totalVendas() > maiorVendas)) {
            maior = "Noite";
            maiorFaturamento = noite.faturamento();
            maiorVendas = noite.totalVendas();
        }

        if (madrugada.faturamento().compareTo(maiorFaturamento) > 0 ||
            (madrugada.faturamento().compareTo(maiorFaturamento) == 0 && madrugada.totalVendas() > maiorVendas)) {
            maior = "Madrugada";
        }

        return maior;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        if (val instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_EVEN);
        }
        if (val instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_EVEN);
        }
        return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_EVEN);
    }

    private int toInt(Object val) {
        if (val == null) {
            return 0;
        }
        if (val instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(val.toString());
    }
}

