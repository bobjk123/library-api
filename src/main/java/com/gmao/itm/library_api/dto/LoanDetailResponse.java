package com.gmao.itm.library_api.dto;

import java.math.BigDecimal;

public record LoanDetailResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Integer quantity,
        BigDecimal subtotalFee
) {}