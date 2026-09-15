package com.mercado.application.usecase;

import com.mercado.application.dto.AmortizarFiadoInput;
import com.mercado.application.dto.AmortizarFiadoOutput;
import com.mercado.domain.entity.Amortizacao;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.AmortizacaoRepository;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Caso de Uso: Amortizar Fiado.
 * Permite abater parte ou a totalidade da dívida de um cliente.
 * Se o pagamento for em DINHEIRO, dá entrada na gaveta do caixa atualmente aberto.
 * Registra o histórico da amortização na tabela de amortizações.
 */
@ApplicationScoped
public class AmortizarFiadoUseCase {

    private final ClienteRepository clienteRepository;
    private final CaixaRepository caixaRepository;
    private final AmortizacaoRepository amortizacaoRepository;

    @Inject
    public AmortizarFiadoUseCase(ClienteRepository clienteRepository,
                                 CaixaRepository caixaRepository,
                                 AmortizacaoRepository amortizacaoRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.amortizacaoRepository = amortizacaoRepository;
    }

    public AmortizarFiadoUseCase(ClienteRepository clienteRepository,
                                 CaixaRepository caixaRepository) {
        this(clienteRepository, caixaRepository, null);
    }

    @Transactional
    public AmortizarFiadoOutput executar(AmortizarFiadoInput input) {
        Objects.requireNonNull(input, "Dados de amortização não podem ser nulos.");

        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para amortizar fiado.");
        }
        if (input.clienteId() == null) {
            throw new RegraDeNegocioException("Identificador do cliente é obrigatório.");
        }
        if (input.valorPago() == null || input.valorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor pago na amortização deve ser estritamente maior que zero.");
        }
        if (input.formaPagamento() == null || input.formaPagamento().isBlank()) {
            throw new RegraDeNegocioException("Forma de pagamento é obrigatória.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Dinheiro valorPago = Dinheiro.de(input.valorPago());
        FormaPagamento formaPagamento = FormaPagamento.de(input.formaPagamento());

        // 1. Busca o cliente
        Cliente cliente = clienteRepository.buscarPorId(tenantId, input.clienteId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado para o mercado especificado."));

        // 2. Busca caixa aberto para o mercado
        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Não existe caixa aberto para o mercado especificado."));

        // 3. Amortiza a dívida do cliente (valida saldo e invariantes no domínio)
        cliente.amortizarDebito(valorPago);

        // 4. Se a forma de pagamento for dinheiro, adiciona na gaveta física do caixa
        if (formaPagamento == FormaPagamento.DINHEIRO) {
            caixa.adicionarDinheiro(valorPago);
            caixaRepository.atualizar(caixa);
        }

        // 5. Atualiza o cliente persistido
        clienteRepository.atualizar(cliente);

        // 6. Registra a amortização para o extrato
        if (amortizacaoRepository != null) {
            Amortizacao amortizacao = Amortizacao.criar(
                tenantId,
                cliente.getId(),
                caixa.getId(),
                valorPago,
                formaPagamento.name()
            );
            amortizacaoRepository.salvar(amortizacao);
        }

        return new AmortizarFiadoOutput(
            cliente.getId(),
            valorPago.valor(),
            cliente.getSaldoDevedor().valor()
        );
    }
}
