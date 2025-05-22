package com.example.books_rental.mapper;

import com.example.books_rental.dto.CreateCategoryDto;
import com.example.books_rental.dto.UpdateCategoryDto;
import com.example.books_rental.model.entities.Category;
import jakarta.validation.Valid;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CreateCategoryDto createCategoryDto);
    Category toCategory(UpdateCategoryDto updateCategoryDto);
}
