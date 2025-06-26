package org.booksrental.bookservice.model.mapper;

import org.booksrental.bookservice.model.dto.CreateCategoryDTO;
import org.booksrental.bookservice.model.dto.UpdateCategoryDTO;
import org.booksrental.bookservice.model.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CreateCategoryDTO createCategoryDto);
    Category toCategory(UpdateCategoryDTO updateCategoryDto);
    CreateCategoryDTO toCreateCategoryDto(Category category);
    UpdateCategoryDTO toUpdateCategoryDto(Category category);
}
