package com.mercado.domain.repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Modelo de domínio puro para encapsular resultados de consultas paginadas.
 * Livre de dependências de frameworks (Spring, Quarkus, JPA).
 *
 * @param <T> Tipo do elemento retornado na página
 */
public record PageResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public PageResult {
        content = content == null ? Collections.emptyList() : Collections.unmodifiableList(content);
    }

    public PageResult(List<T> content, int page, int size, long totalElements) {
        this(
            content,
            page,
            size,
            totalElements,
            size > 0 ? (int) Math.ceil((double) totalElements / size) : 0
        );
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return totalPages == 0 || page >= totalPages - 1;
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "Mapper não pode ser nulo.");
        List<R> mappedContent = this.content.stream().map(mapper).toList();
        return new PageResult<>(mappedContent, this.page, this.size, this.totalElements, this.totalPages);
    }
}
