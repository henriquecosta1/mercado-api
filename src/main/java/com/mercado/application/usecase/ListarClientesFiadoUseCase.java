package com.mercado.application.usecase;

import com.mercado.application.dto.ClienteDTO;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Listar Clientes com saldo devedor (fiado).
 * Permite filtrar opcionalmente por termo de busca no nome.
 */
@ApplicationScoped
public class ListarClientesFiadoUseCase {

    private final ClienteRepository clienteRepository;

    @Inject
    public ListarClientesFiadoUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
    }

    public List<ClienteDTO> executar(UUID tenantIdUuid, String busca) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar clientes fiado.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        List<Cliente> clientes;
        if (busca != null && !busca.isBlank()) {
            clientes = clienteRepository.buscarPorNome(busca.trim(), tenantId)
                .stream()
                .filter(c -> c.getSaldoDevedor().isMaiorQue(Dinheiro.zero()))
                .toList();
        } else {
            clientes = clienteRepository.listarComSaldoDevedor(tenantId);
        }

        return clientes.stream()
            .map(ClienteDTO::from)
            .toList();
    }
}
