package com.gmao.itm.library_api.mapper;

import com.gmao.itm.library_api.dto.CategoryRequest;
import com.gmao.itm.library_api.dto.CategoryResponse;
import com.gmao.itm.library_api.model.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category entity);

    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category entity);
}
