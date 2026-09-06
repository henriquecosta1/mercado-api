package com.mercado.application.usecase;

import com.mercado.application.dto.CancelarVendaInput;
import com.mercado.application.dto.CancelarVendaOutput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Cancelamento e Estorno de Venda.
 * Executa as seguintes regras de negócio em transação única:
 * 1. Valida se a venda existe e pertence ao tenant.
 * 2. Valida se a venda já está cancelada.
 * 3. Valida se o caixa associado à venda ainda se encontra aberto.
 * 4. Se DINHEIRO, subtrai o valor do saldo em dinheiro do caixa.
 * 5. Se FIADO, estorna o saldo devedor do cliente.
 * 6. Repõe o estoque dos produtos que compunham os itens da venda.
 * 7. Marca a venda como CANCELADA com data e motivo.
 */
@ApplicationScoped
public class CancelarVendaUseCase {

    private final VendaRepository vendaRepository;
    private final CaixaRepository caixaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    @Inject
    public CancelarVendaUseCase(VendaRepository vendaRepository,
                                CaixaRepository caixaRepository,
                                ClienteRepository clienteRepository,
                                ProdutoRepository produtoRepository) {
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.produtoRepository = produtoRepository;
    }

    public CancelarVendaUseCase(VendaRepository vendaRepository,
                                CaixaRepository caixaRepository,
                                ClienteRepository clienteRepository) {
        this(vendaRepository, caixaRepository, clienteRepository, null);
    }

    @Transactional
    public CancelarVendaOutput executar(CancelarVendaInput input) {
        Objects.requireNonNull(input, "Dados de entrada para cancelamento não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para cancelar a venda.");
        }
        if (input.vendaId() == null) {
            throw new RegraDeNegocioException("Id da venda é obrigatório para cancelamento.");
        }
        if (input.motivo() == null || input.motivo().isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());

        // 1. Busca venda com seus itens
        Venda venda = vendaRepository.buscarPorIdComItens(input.vendaId(), tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Venda não encontrada com id: " + input.vendaId()));

        // 2. Validação se já cancelada
        if (venda.isCancelada()) {
            throw new RegraDeNegocioException("Venda já se encontra cancelada.");
        }

        // 3. Validação do Caixa: só permite estorno de caixa ainda ABERTO
        Caixa caixa = caixaRepository.buscarPorId(tenantId, venda.getCaixaId())
            .orElseThrow(() -> new RegraDeNegocioException("Caixa associado à venda não foi encontrado."));

        if (!caixa.isAberto()) {
            throw new RegraDeNegocioException("Não é permitido estornar venda de um caixa já encerrado.");
        }

        // 4. Estorno financeiro / conta corrente
        if (venda.getFormaPagamento() == FormaPagamento.DINHEIRO) {
            caixa.estornarVendaDinheiro(venda.getValorTotal());
            caixaRepository.atualizar(caixa);
        } else if (venda.getFormaPagamento() == FormaPagamento.FIADO) {
            if (venda.getClienteId() != null) {
                clienteRepository.buscarPorId(tenantId, venda.getClienteId()).ifPresent(cliente -> {
                    cliente.estornarDebito(venda.getValorTotal());
                    clienteRepository.atualizar(cliente);
                });
            } else if (venda.getNomeCliente() != null && !venda.getNomeCliente().isBlank()) {
                clienteRepository.buscarPorNomeOuTelefone(tenantId, venda.getNomeCliente(), null).ifPresent(cliente -> {
                    cliente.estornarDebito(venda.getValorTotal());
                    clienteRepository.atualizar(cliente);
                });
            }
        }

        // 5. Reposição de estoque dos itens
        if (produtoRepository != null && venda.getItens() != null && !venda.getItens().isEmpty()) {
            for (ItemVenda item : venda.getItens()) {
                if (item.getProdutoId() != null) {
                    produtoRepository.buscarPorId(item.getProdutoId(), tenantId).ifPresent(produto -> {
                        produto.reporEstoque(item.getQuantidade());
                        produtoRepository.salvar(produto);
                    });
                }
            }
        }

        // 6. Atualiza status da venda
        venda.cancelar(input.motivo());
        vendaRepository.salvar(venda);

        return new CancelarVendaOutput(
            venda.getId(),
            venda.getStatus().name(),
            venda.getCanceladaEm()
        );
    }
}
