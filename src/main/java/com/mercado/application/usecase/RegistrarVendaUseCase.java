package com.mercado.application.usecase;

import com.mercado.application.dto.RegistrarVendaInput;
import com.mercado.application.dto.RegistrarVendaOutput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Caso de Uso: Registrar Venda.
 * Orquestra as regras de negócio aplicando os princípios SOLID e Clean Architecture.
 */
@ApplicationScoped
public class RegistrarVendaUseCase {

    private final CaixaRepository caixaRepository;
    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;

    @Inject
    public RegistrarVendaUseCase(CaixaRepository caixaRepository,
                                 ClienteRepository clienteRepository,
                                 VendaRepository vendaRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
    }

    @Transactional
    public RegistrarVendaOutput executar(RegistrarVendaInput input) {
        Objects.requireNonNull(input, "Dados de entrada da venda não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para registrar a venda.");
        }
        if (input.valorTotal() == null || input.valorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor total da venda deve ser estritamente maior que zero.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Dinheiro valorTotal = Dinheiro.de(input.valorTotal());
        FormaPagamento formaPagamento = FormaPagamento.de(input.formaPagamento());

        // 1. Busca caixa aberto para o tenant
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Não existe caixa aberto para o tenant especificado."));

        Dinheiro troco = Dinheiro.zero();
        BigDecimal saldoDevedorCliente = null;

        // 2. Regras específicas por forma de pagamento
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
            }
            case PIX, CARTAO -> {
                // Pagamentos eletrônicos não alteram gaveta de dinheiro nem geram débito de cliente
                troco = Dinheiro.zero();
            }
        }

        // 3. Registra a venda
        Venda venda = Venda.criar(
            tenantId,
            caixa.getId(),
            valorTotal,
            formaPagamento,
            troco,
            input.descricao()
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
