package com.mercado.application.usecase;

import com.mercado.application.dto.ClienteDetalhadoDTO;
import com.mercado.application.dto.SalvarClienteInput;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Cadastrar ou atualizar cliente completo.
 */
@ApplicationScoped
public class SalvarClienteUseCase {

    private final ClienteRepository clienteRepository;

    @Inject
    public SalvarClienteUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository e obrigatorio.");
    }

    @Transactional
    public ClienteDetalhadoDTO executar(SalvarClienteInput input) {
        Objects.requireNonNull(input, "Dados de entrada do cliente nao podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId e obrigatorio.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());

        if (input.id() == null) {
            // Criacao de novo cliente
            Dinheiro limite = input.limiteCredito() != null ? Dinheiro.de(input.limiteCredito()) : Dinheiro.zero();
            Cliente novoCliente = Cliente.criar(
                tenantId,
                input.nome(),
                input.apelido(),
                input.telefone(),
                input.cpf(),
                input.endereco(),
                input.pontoReferencia(),
                limite,
                input.diaVencimento(),
                input.observacoes()
            );

            clienteRepository.salvar(novoCliente);
            return ClienteDetalhadoDTO.from(novoCliente);
        } else {
            // Atualizacao de cliente existente
            Cliente clienteExistente = clienteRepository.buscarPorId(tenantId, input.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + input.id()));

            Dinheiro limite = input.limiteCredito() != null ? Dinheiro.de(input.limiteCredito()) : null;
            clienteExistente.atualizarDados(
                input.nome(),
                input.apelido(),
                input.telefone(),
                input.cpf(),
                input.endereco(),
                input.pontoReferencia(),
                limite,
                input.diaVencimento(),
                input.observacoes()
            );

            clienteRepository.atualizar(clienteExistente);
            return ClienteDetalhadoDTO.from(clienteExistente);
        }
    }
}