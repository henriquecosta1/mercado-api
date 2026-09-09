package com.mercado.application.usecase;

import com.mercado.application.dto.ExtratoClienteOutput;
import com.mercado.application.dto.ItemExtratoDTO;
import com.mercado.application.dto.TransacaoExtratoDTO;
import com.mercado.domain.entity.Amortizacao;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.AmortizacaoRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Obter Extrato Detalhado e Linha do Tempo do Cliente Fiado.
 * Consolida compras a prazo com itens e pagamentos/amortizações ordenados decrescentemente.
 */
@ApplicationScoped
public class ObterExtratoClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final AmortizacaoRepository amortizacaoRepository;

    @Inject
    public ObterExtratoClienteUseCase(ClienteRepository clienteRepository,
                                     VendaRepository vendaRepository,
                                     AmortizacaoRepository amortizacaoRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
        this.amortizacaoRepository = Objects.requireNonNull(amortizacaoRepository, "AmortizacaoRepository é obrigatório.");
    }

    public ExtratoClienteOutput executar(UUID tenantIdUuid, UUID clienteId) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para obter o extrato.");
        }
        if (clienteId == null) {
            throw new IllegalArgumentException("Id do cliente é obrigatório para obter o extrato.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        // 1. Busca os dados do cliente
        Cliente cliente = clienteRepository.buscarPorId(tenantId, clienteId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com id: " + clienteId));

        // 2. Busca histórico de compras fiado
        List<Venda> vendas = vendaRepository.listarFiadoPorCliente(clienteId, tenantId);

        // 3. Busca histórico de amortizações
        List<Amortizacao> amortizacoes = amortizacaoRepository.listarPorCliente(clienteId, tenantId);

        // 4. Converte e unifica na linha do tempo
        List<TransacaoExtratoDTO> transacoes = new ArrayList<>();

        for (Venda v : vendas) {
            List<ItemExtratoDTO> itens = v.getItens() != null
                ? v.getItens().stream()
                    .map(i -> new ItemExtratoDTO(i.getDescricao(), i.getQuantidade(), i.getSubtotal().valor()))
                    .toList()
                : Collections.emptyList();

            transacoes.add(new TransacaoExtratoDTO(
                v.getId(),
                "COMPRA",
                v.getCriadoEm(),
                v.getValorTotal().valor(),
                v.getFormaPagamento().name(),
                itens
            ));
        }

        for (Amortizacao a : amortizacoes) {
            transacoes.add(new TransacaoExtratoDTO(
                a.getId(),
                "PAGAMENTO",
                a.getCriadoEm(),
                a.getValor().valor(),
                a.getFormaPagamento(),
                Collections.emptyList()
            ));
        }

        // 5. Ordena da transação mais recente para a mais antiga
        transacoes.sort(Comparator.comparing(TransacaoExtratoDTO::dataHora).reversed());

        return new ExtratoClienteOutput(
            cliente.getId(),
            cliente.getNome(),
            cliente.getTelefone(),
            cliente.getSaldoDevedor().valor(),
            cliente.getLimiteCredito().valor(),
            transacoes
        );
    }
}
