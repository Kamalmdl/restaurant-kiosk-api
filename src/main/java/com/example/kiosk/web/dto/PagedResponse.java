package com.example.kiosk.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Generic page wrapper: the items of the current page plus paging metadata. */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}