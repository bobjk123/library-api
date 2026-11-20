package com.gmao.itm.library_api.mapper;

import com.gmao.itm.library_api.dto.MemberRequest;
import com.gmao.itm.library_api.dto.MemberResponse;
import com.gmao.itm.library_api.model.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loans", ignore = true)
    Member toEntity(MemberRequest request);

    MemberResponse toResponse(Member entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loans", ignore = true)
    void updateEntityFromRequest(MemberRequest request, @MappingTarget Member entity);
}