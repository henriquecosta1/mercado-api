package com.mercado.api.resource;

import com.mercado.application.usecase.*;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST para /clientes, garantindo compatibilidade com clientes que chamam
 * /clientes ou /api/clientes.
 */
@Path("/clientes")
public class ClientesResource extends ClienteResource {

    @Inject
    public ClientesResource(ListarClientesUseCase listarClientesUseCase,
                            ListarClientesFiadoUseCase listarClientesFiadoUseCase,
                            ExcluirClienteUseCase excluirClienteUseCase,
                            AmortizarFiadoUseCase amortizarFiadoUseCase,
                            ObterExtratoClienteUseCase obterExtratoClienteUseCase,
                            SalvarClienteUseCase salvarClienteUseCase,
                            AlterarStatusClienteUseCase alterarStatusClienteUseCase,
                            ClienteRepository clienteRepository,
                            TenantSecurityContext securityContext) {
        super(listarClientesUseCase,
              listarClientesFiadoUseCase,
              excluirClienteUseCase,
              amortizarFiadoUseCase,
              obterExtratoClienteUseCase,
              salvarClienteUseCase,
              alterarStatusClienteUseCase,
              clienteRepository,
              securityContext);
    }
}