package com.mercado.application.usecase;

import com.mercado.application.dto.ValidarPinInput;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.exception.ClienteComDebitoException;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Excluir Cliente do sistema.
 * 1. Valida se o cliente existe e pertence ao Tenant.
 * 2. Valida se possui débito pendente (saldo devedor > 0). Se possuir, impede a exclusão lançando ClienteComDebitoException.
 * 3. Valida PIN do gerente caso fornecido.
 * 4. Executa a exclusão no repositório.
 */
@ApplicationScoped
public class ExcluirClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final ValidarPinGerenteUseCase validarPinGerenteUseCase;

    @Inject
    public ExcluirClienteUseCase(ClienteRepository clienteRepository,
                                 ValidarPinGerenteUseCase validarPinGerenteUseCase) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.validarPinGerenteUseCase = validarPinGerenteUseCase;
    }

    public ExcluirClienteUseCase(ClienteRepository clienteRepository) {
        this(clienteRepository, null);
    }

    @Transactional
    public void executar(UUID tenantIdUuid, UUID clienteId, String pin) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para exclusão.");
        }
        if (clienteId == null) {
            throw new RegraDeNegocioException("Id do cliente é obrigatório para exclusão.");
        }

        if (pin != null && !pin.isBlank() && validarPinGerenteUseCase != null) {
            validarPinGerenteUseCase.executar(new ValidarPinInput(tenantIdUuid, pin));
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        Cliente cliente = clienteRepository.buscarPorId(tenantId, clienteId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com id: " + clienteId));

        if (!cliente.podeSerExcluido()) {
            throw new ClienteComDebitoException("Não é possível excluir um cliente que possui saldo devedor pendente.");
        }

        clienteRepository.excluir(clienteId, tenantId);
    }

    @Transactional
    public void executar(UUID tenantIdUuid, UUID clienteId) {
        executar(tenantIdUuid, clienteId, null);
    }
}
