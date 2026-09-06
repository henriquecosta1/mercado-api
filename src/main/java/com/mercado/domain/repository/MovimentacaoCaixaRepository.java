package com.mercado.domain.repository;

import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.valueobject.TenantId;

import java.util.List;
import java.util.UUID;

/**
 * Interface pura de repositório para MovimentacaoCaixa (DIP - Dependency Inversion Principle).
 */
public interface MovimentacaoCaixaRepository {

    void salvar(MovimentacaoCaixa mov);

    List<MovimentacaoCaixa> listarPorCaixa(UUID caixaId, TenantId tenantId);
}
