package com.gmao.itm.library_api.dto;

import java.math.BigDecimal;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        Integer totalCopies,
        Integer availableCopies,
        BigDecimal lateFeePerDay,
        Long categoryId,
        String categoryName
) {}