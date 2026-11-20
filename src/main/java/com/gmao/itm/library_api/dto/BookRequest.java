package com.gmao.itm.library_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BookRequest(
        @NotBlank(message = "ISBN is required")
        @Size(max = 20, message = "ISBN must not exceed 20 characters")
        String isbn,

        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title must not exceed 180 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 140, message = "Author must not exceed 140 characters")
        String author,

        @NotNull(message = "Total copies is required")
        @Min(value = 1, message = "Total copies must be at least 1")
        Integer totalCopies,

        @NotNull(message = "Available copies is required")
        @Min(value = 0, message = "Available copies cannot be negative")
        Integer availableCopies,

        @NotNull(message = "Late fee per day is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Late fee per day cannot be negative")
        BigDecimal lateFeePerDay,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {}
