package com.mercado.application.usecase;

import com.mercado.application.dto.ItemVendaInput;
import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.domain.entity.*;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Registrar Venda.
 * Orquestra as regras de negócio aplicando os princípios SOLID e Clean Architecture.
 * Suporta venda avulsa direta ou venda detalhada com itens de carrinho e baixa de estoque.
 */
@ApplicationScoped
public class RegistrarVendaUseCase {

    private final CaixaRepository caixaRepository;
    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;

    @Inject
    public RegistrarVendaUseCase(CaixaRepository caixaRepository,
                                 ClienteRepository clienteRepository,
                                 VendaRepository vendaRepository,
                                 ProdutoRepository produtoRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
        this.produtoRepository = produtoRepository;
    }

    public RegistrarVendaUseCase(CaixaRepository caixaRepository,
                                 ClienteRepository clienteRepository,
                                 VendaRepository vendaRepository) {
        this(caixaRepository, clienteRepository, vendaRepository, null);
    }

    @Transactional
    public RegistrarVendaOutput executar(RegistrarVendaInput input) {
        Objects.requireNonNull(input, "Dados de entrada da venda não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para registrar a venda.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        FormaPagamento formaPagamento = FormaPagamento.de(input.formaPagamento());

        // 1. Processa itens da venda (se informados) e valida total
        List<ItemVenda> itensDominio = new ArrayList<>();
        Dinheiro valorTotal;

        if (input.itens() != null && !input.itens().isEmpty()) {
            BigDecimal somaItens = BigDecimal.ZERO;

            for (ItemVendaInput itemInput : input.itens()) {
                if (itemInput.quantidade() == null || itemInput.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RegraDeNegocioException("Quantidade do item deve ser maior que zero.");
                }
                if (itemInput.precoUnitario() == null || itemInput.precoUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RegraDeNegocioException("Preço unitário do item deve ser maior que zero.");
                }

                // Baixa de estoque se produtoId estiver associado
                if (itemInput.produtoId() != null && produtoRepository != null) {
                    Produto produto = produtoRepository.buscarPorId(itemInput.produtoId(), tenantId)
                        .orElseThrow(() -> new RegraDeNegocioException("Produto não encontrado com id: " + itemInput.produtoId()));
                    produto.baixarEstoque(itemInput.quantidade());
                    produtoRepository.salvar(produto);
                }

                ItemVenda item = ItemVenda.criar(
                    itemInput.produtoId(),
                    itemInput.descricao(),
                    itemInput.quantidade(),
                    Dinheiro.de(itemInput.precoUnitario())
                );
                itensDominio.add(item);
                somaItens = somaItens.add(item.getSubtotal().valor());
            }

            if (input.valorTotal() != null) {
                if (input.valorTotal().compareTo(somaItens) != 0) {
                    throw new RegraDeNegocioException(
                        "Valor total informado (" + input.valorTotal() + ") difere da soma dos itens (" + somaItens + ")."
                    );
                }
                valorTotal = Dinheiro.de(input.valorTotal());
            } else {
                valorTotal = Dinheiro.de(somaItens);
            }
        } else {
            if (input.valorTotal() == null || input.valorTotal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraDeNegocioException("Valor total da venda deve ser estritamente maior que zero.");
            }
            valorTotal = Dinheiro.de(input.valorTotal());
        }

        // 2. Busca caixa aberto para o tenant
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Não existe caixa aberto para o tenant especificado."));

        Dinheiro troco = Dinheiro.zero();
        BigDecimal saldoDevedorCliente = null;
        UUID clienteId = null;
        String nomeCliente = (input.nomeClienteFiado() != null && !input.nomeClienteFiado().isBlank())
            ? input.nomeClienteFiado().trim()
            : null;

        // 3. Regras específicas por forma de pagamento
        switch (formaPagamento) {
            case DINHEIRO -> {
                if (input.valorRecebido() == null) {
                    throw new RegraDeNegocioException("Valor recebido é obrigatório para pagamento em DINHEIRO.");
                }
                Dinheiro valorRecebido = Dinheiro.de(input.valorRecebido());
                if (valorRecebido.isMenorQue(valorTotal)) {
                    throw new RegraDeNegocioException(
                        "Valor recebido (" + valorRecebido + ") é insuficiente para pagar o total (" + valorTotal + ")."
                    );
                }
                troco = valorRecebido.subtrair(valorTotal);

                // Incrementa gaveta do caixa com o valor da venda
                caixa.adicionarDinheiro(valorTotal);
                caixaRepository.atualizar(caixa);
            }
            case FIADO -> {
                if (input.nomeClienteFiado() == null || input.nomeClienteFiado().isBlank()) {
                    throw new RegraDeNegocioException("Nome do cliente é obrigatório para vendas na modalidade FIADO.");
                }

                String nome = input.nomeClienteFiado().trim();
                String telefone = input.telefoneClienteFiado() != null ? input.telefoneClienteFiado().trim() : null;

                Cliente cliente = clienteRepository.buscarPorNomeOuTelefone(tenantId, nome, telefone)
                    .orElseGet(() -> {
                        Cliente novoCliente = Cliente.criar(tenantId, nome, telefone, Dinheiro.zero());
                        clienteRepository.salvar(novoCliente);
                        return novoCliente;
                    });

                cliente.registrarDebito(valorTotal);
                clienteRepository.atualizar(cliente);
                saldoDevedorCliente = cliente.getSaldoDevedor().valor();
                clienteId = cliente.getId();
                nomeCliente = cliente.getNome();
            }
            case PIX, CARTAO -> {
                troco = Dinheiro.zero();
            }
        }

        // 4. Registra a venda (com itens, se houver)
        Venda venda = Venda.criar(
            tenantId,
            caixa.getId(),
            valorTotal,
            formaPagamento,
            troco,
            input.descricao(),
            itensDominio,
            clienteId,
            nomeCliente
        );
        vendaRepository.salvar(venda);

        return new RegistrarVendaOutput(
            venda.getId(),
            valorTotal.valor(),
            troco.valor(),
            saldoDevedorCliente
        );
    }
}
