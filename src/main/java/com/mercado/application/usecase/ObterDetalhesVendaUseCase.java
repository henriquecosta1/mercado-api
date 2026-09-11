package com.mercado.application.usecase;

import com.mercado.application.dto.ClienteVendaDTO;
import com.mercado.application.dto.ItemVendaDetalheDTO;
import com.mercado.application.dto.VendaDetalheResponseDTO;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.ItemVenda;
import com.mercado.domain.entity.Produto;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de Uso: Obter Detalhes Completos da Venda (com itens e dados do cliente para F3).
 */
@ApplicationScoped
public class ObterDetalhesVendaUseCase {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    @Inject
    public ObterDetalhesVendaUseCase(VendaRepository vendaRepository,
                                     ClienteRepository clienteRepository,
                                     ProdutoRepository produtoRepository) {
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository é obrigatório.");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    public VendaDetalheResponseDTO executar(UUID tenantIdUuid, UUID vendaId) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório.");
        }
        if (vendaId == null) {
            throw new IllegalArgumentException("Id da venda é obrigatório.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);
        Venda venda = vendaRepository.buscarPorIdComItens(vendaId, tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada para o ID informado: " + vendaId));

        ClienteVendaDTO clienteDTO = null;
        if (venda.getClienteId() != null) {
            Optional<Cliente> clienteOpt = clienteRepository.buscarPorId(tenantId, venda.getClienteId());
            if (clienteOpt.isPresent()) {
                Cliente c = clienteOpt.get();
                clienteDTO = new ClienteVendaDTO(c.getId(), c.getNome(), c.getTelefone());
            } else {
                clienteDTO = new ClienteVendaDTO(venda.getClienteId(), venda.getNomeCliente(), null);
            }
        } else if (venda.getNomeCliente() != null && !venda.getNomeCliente().isBlank()) {
            clienteDTO = new ClienteVendaDTO(null, venda.getNomeCliente(), null);
        }

        List<ItemVendaDetalheDTO> itensDTO = new ArrayList<>();
        if (venda.getItens() != null) {
            for (ItemVenda item : venda.getItens()) {
                String unidadeMedida = "UN";
                if (item.getProdutoId() != null) {
                    Optional<Produto> prodOpt = produtoRepository.buscarPorId(item.getProdutoId(), tenantId);
                    if (prodOpt.isPresent() && prodOpt.get().getUnidade() != null && !prodOpt.get().getUnidade().isBlank()) {
                        unidadeMedida = prodOpt.get().getUnidade();
                    }
                }

                itensDTO.add(new ItemVendaDetalheDTO(
                    item.getProdutoId(),
                    item.getDescricao(),
                    unidadeMedida,
                    item.getQuantidade(),
                    item.getPrecoUnitario().valor(),
                    item.getSubtotal().valor()
                ));
            }
        }

        return new VendaDetalheResponseDTO(
            venda.getId(),
            venda.getCriadoEm(),
            venda.getValorTotal().valor(),
            venda.getFormaPagamento().name(),
            venda.getStatus().name(),
            clienteDTO,
            itensDTO
        );
    }
}
