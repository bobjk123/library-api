package com.gmao.itm.library_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record LoanRequest(
        @NotNull(message = "Member ID is required")
        Long memberId,

        @NotNull(message = "Loan date is required")
        LocalDate loanDate,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @NotEmpty(message = "Loan must contain at least one detail item")
        @Valid
        List<LoanDetailRequest> details
) {}