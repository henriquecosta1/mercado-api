package com.mercado.application.usecase;

import com.mercado.application.dto.ClienteDTO;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Listar clientes com suporte a filtros para o módulo de Gestão de Clientes (CRUD),
 * autocomplete do PDV e visão do Caderninho (apenasDevedores).
 */
@ApplicationScoped
public class ListarClientesUseCase {

    private final ClienteRepository clienteRepository;

    @Inject
    public ListarClientesUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
    }

    public List<ClienteDTO> executar(UUID tenantIdUuid, String busca, String status, Boolean apenasDevedores) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar clientes.");
        }
        return executar(TenantId.de(tenantIdUuid), busca, status, apenasDevedores);
    }

    public List<ClienteDTO> executar(TenantId tenantId, String busca, String status, Boolean apenasDevedores) {
        if (tenantId == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar clientes.");
        }

        List<Cliente> clientes = clienteRepository.listarTodos(tenantId, busca, status, apenasDevedores);

        return clientes.stream()
            .map(ClienteDTO::from)
            .toList();
    }

    public com.mercado.application.dto.PageDTO<ClienteDTO> executarPaginado(UUID tenantIdUuid, String busca, String status, Boolean apenasDevedores, int page, int size) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar clientes.");
        }
        return executarPaginado(TenantId.de(tenantIdUuid), busca, status, apenasDevedores, page, size);
    }

    public com.mercado.application.dto.PageDTO<ClienteDTO> executarPaginado(TenantId tenantId, String busca, String status, Boolean apenasDevedores, int page, int size) {
        if (tenantId == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar clientes.");
        }

        int paginaEfetiva = Math.max(0, page);
        int tamanhoEfetivo = Math.min(Math.max(1, size), 100);

        com.mercado.domain.repository.PageResult<Cliente> pageResult = clienteRepository.listarTodosPaginado(
            tenantId, busca, status, apenasDevedores, paginaEfetiva, tamanhoEfetivo
        );

        List<ClienteDTO> dtos = pageResult.content().stream()
            .map(ClienteDTO::from)
            .toList();

        return com.mercado.application.dto.PageDTO.of(dtos, pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
