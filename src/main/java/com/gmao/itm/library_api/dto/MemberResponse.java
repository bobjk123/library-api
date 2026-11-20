package com.gmao.itm.library_api.dto;

import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        LocalDate registrationDate,
        Boolean active
) {}