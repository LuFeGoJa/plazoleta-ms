package com.pragma.powerup.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    /** Número de página actual, empezando en 0. */
    private int page;
    /** Cantidad de elementos pedidos por página. */
    private int size;
    private long totalElements;
    private int totalPages;
}
