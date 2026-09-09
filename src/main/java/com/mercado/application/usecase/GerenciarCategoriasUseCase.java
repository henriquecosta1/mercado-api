package com.mercado.application.usecase;

import com.mercado.application.dto.AtualizarCategoriaInput;
import com.mercado.application.dto.CategoriaDTO;
import com.mercado.application.dto.CriarCategoriaInput;
import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CategoriaProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de Uso: Gerenciamento completo de Categorias de Produtos customizadas por Tenant (CRUD).
 */
@ApplicationScoped
public class GerenciarCategoriasUseCase {

    private final CategoriaProdutoRepository categoriaProdutoRepository;

    @Inject
    public GerenciarCategoriasUseCase(CategoriaProdutoRepository categoriaProdutoRepository) {
        this.categoriaProdutoRepository = Objects.requireNonNull(categoriaProdutoRepository, "CategoriaProdutoRepository é obrigatório.");
    }

    public List<CategoriaDTO> listar(UUID tenantIdUuid) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar categorias.");
        }
        TenantId tenantId = TenantId.de(tenantIdUuid);
        return categoriaProdutoRepository.listarPorTenant(tenantId)
            .stream()
            .map(CategoriaDTO::from)
            .toList();
    }

    public CategoriaDTO buscarPorId(UUID tenantIdUuid, UUID id) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório.");
        }
        if (id == null) {
            throw new RegraDeNegocioException("Id da categoria é obrigatório.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);
        CategoriaProduto categoria = categoriaProdutoRepository.buscarPorId(id, tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));

        return CategoriaDTO.from(categoria);
    }

    @Transactional
    public CategoriaDTO criar(CriarCategoriaInput input) {
        Objects.requireNonNull(input, "Dados de criação da categoria não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório.");
        }
        if (input.nome() == null || input.nome().isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria é obrigatório.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Optional<CategoriaProduto> existente = categoriaProdutoRepository.buscarPorNome(input.nome().trim(), tenantId);
        if (existente.isPresent()) {
            CategoriaProduto cat = existente.get();
            if (!cat.isAtivo()) {
                cat.ativar();
                cat.atualizar(input.nome().trim(), input.icone());
                categoriaProdutoRepository.salvar(cat);
                return CategoriaDTO.from(cat);
            }
            throw new RegraDeNegocioException("Já existe uma categoria cadastrada com o nome '" + input.nome().trim() + "'.");
        }

        CategoriaProduto novaCategoria = CategoriaProduto.criar(tenantId, input.nome(), input.icone());
        categoriaProdutoRepository.salvar(novaCategoria);
        return CategoriaDTO.from(novaCategoria);
    }

    @Transactional
    public CategoriaDTO atualizar(AtualizarCategoriaInput input) {
        Objects.requireNonNull(input, "Dados de atualização da categoria não podem ser nulos.");
        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório.");
        }
        if (input.id() == null) {
            throw new RegraDeNegocioException("Id da categoria é obrigatório.");
        }
        if (input.nome() == null || input.nome().isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria é obrigatório.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        CategoriaProduto categoria = categoriaProdutoRepository.buscarPorId(input.id(), tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + input.id()));

        Optional<CategoriaProduto> existente = categoriaProdutoRepository.buscarPorNome(input.nome().trim(), tenantId);
        if (existente.isPresent() && !existente.get().getId().equals(input.id())) {
            throw new RegraDeNegocioException("Já existe outra categoria cadastrada com o nome '" + input.nome().trim() + "'.");
        }

        categoria.atualizar(input.nome(), input.icone());
        categoriaProdutoRepository.salvar(categoria);
        return CategoriaDTO.from(categoria);
    }

    @Transactional
    public void excluir(UUID tenantIdUuid, UUID id) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório.");
        }
        if (id == null) {
            throw new RegraDeNegocioException("Id da categoria é obrigatório.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);
        CategoriaProduto categoria = categoriaProdutoRepository.buscarPorId(id, tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));

        if (categoriaProdutoRepository.existeVinculoComProduto(id, tenantId)) {
            throw new RegraDeNegocioException("Não é possível excluir uma categoria que possui produtos vinculados.");
        }

        categoriaProdutoRepository.excluir(id, tenantId);
    }
}
