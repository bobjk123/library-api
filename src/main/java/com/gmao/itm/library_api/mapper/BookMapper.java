package com.gmao.itm.library_api.mapper;

import com.gmao.itm.library_api.dto.BookRequest;
import com.gmao.itm.library_api.dto.BookResponse;
import com.gmao.itm.library_api.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Book toEntity(BookRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    BookResponse toResponse(Book entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(BookRequest request, @MappingTarget Book entity);
}