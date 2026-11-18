package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Página de resultados independiente de Spring: así el dominio no conoce a Spring Data. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PageModel<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
