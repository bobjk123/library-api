package com.gmao.itm.library_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must have a valid format")
        @Size(max = 120, message = "Email must not exceed 120 characters")
        String email,

        @Size(max = 25, message = "Phone must not exceed 25 characters")
        String phone,

        @NotNull(message = "Registration date is required")
        LocalDate registrationDate,

        @NotNull(message = "Active flag is required")
        Boolean active
) {}