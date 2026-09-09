package com.mercado.application.usecase;

import com.mercado.application.dto.AlterarStatusClienteInput;
import com.mercado.application.dto.ClienteDetalhadoDTO;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Alterar o status cadastral do cliente (ATIVO, BLOQUEADO, INATIVO).
 */
@ApplicationScoped
public class AlterarStatusClienteUseCase {

    private final ClienteRepository clienteRepository;

    @Inject
    public AlterarStatusClienteUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository e obrigatorio.");
    }

    @Transactional
    public ClienteDetalhadoDTO executar(AlterarStatusClienteInput input) {
        Objects.requireNonNull(input, "Dados de alteracao de status nao podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId e obrigatorio.");
        }
        if (input.clienteId() == null) {
            throw new RegraDeNegocioException("Id do cliente e obrigatorio.");
        }
        if (input.status() == null) {
            throw new RegraDeNegocioException("Novo status e obrigatorio.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Cliente cliente = clienteRepository.buscarPorId(tenantId, input.clienteId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + input.clienteId()));

        switch (input.status()) {
            case BLOQUEADO -> cliente.bloquear(input.motivo());
            case ATIVO -> cliente.desbloquear();
            case INATIVO -> cliente.inativar();
        }

        clienteRepository.atualizar(cliente);
        return ClienteDetalhadoDTO.from(cliente);
    }
}