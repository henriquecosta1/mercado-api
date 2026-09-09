package com.mercado.application.dto;

import com.mercado.domain.repository.PageResult;
import java.util.Collections;
import java.util.List;

/**
 * DTO genérico de resposta para Server-Side Pagination na API REST.
 * Retorna o conteúdo da página atual acompanhado de metadados de paginação.
 *
 * @param <T> Tipo do item serializado
 */
public record PageDTO<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public PageDTO {
        content = content == null ? Collections.emptyList() : Collections.unmodifiableList(content);
    }

    public static <T> PageDTO<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageDTO<>(
            content,
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            totalPages == 0 || page >= totalPages - 1
        );
    }

    public static <T> PageDTO<T> from(PageResult<T> pageResult) {
        return of(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        );
    }
}
