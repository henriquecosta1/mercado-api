package com.mercado.domain.repository;

import com.mercado.domain.entity.Amortizacao;
import com.mercado.domain.valueobject.TenantId;

import java.util.List;
import java.util.UUID;

/**
 * Interface pura de repositório para a entidade Amortizacao (DIP - Dependency Inversion Principle).
 */
public interface AmortizacaoRepository {

    void salvar(Amortizacao amortizacao);

    List<Amortizacao> listarPorCliente(UUID clienteId, TenantId tenantId);
}
