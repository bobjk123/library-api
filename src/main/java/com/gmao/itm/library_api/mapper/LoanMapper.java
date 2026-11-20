package com.gmao.itm.library_api.mapper;

import com.gmao.itm.library_api.dto.LoanDetailResponse;
import com.gmao.itm.library_api.dto.LoanResponse;
import com.gmao.itm.library_api.model.Loan;
import com.gmao.itm.library_api.model.LoanDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    LoanDetailResponse toDetailResponse(LoanDetail detail);

    List<LoanDetailResponse> toDetailResponseList(List<LoanDetail> details);

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "details", target = "details")
    LoanResponse toResponse(Loan loan);
}