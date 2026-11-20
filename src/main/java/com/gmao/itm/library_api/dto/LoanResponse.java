package com.gmao.itm.library_api.dto;

import com.gmao.itm.library_api.model.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LoanResponse(
        Long id,
        Long memberId,
        String memberName,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        LoanStatus status,
        BigDecimal estimatedFee,
        List<LoanDetailResponse> details
) {}
