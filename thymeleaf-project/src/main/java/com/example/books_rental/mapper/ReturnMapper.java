package com.example.books_rental.mapper;

import com.example.books_rental.dto.ReturnDto;
import com.example.books_rental.model.entities.Return;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReturnMapper {
    Return toReturn(ReturnDto returnDto);
}
