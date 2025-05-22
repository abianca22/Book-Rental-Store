package com.example.books_rental.mapper;

import com.example.books_rental.dto.CreateBookDto;
import com.example.books_rental.dto.UpdateBookDto;
import com.example.books_rental.model.entities.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toBook(CreateBookDto createBookDto);
    Book toBook(UpdateBookDto updateBookDto);
    UpdateBookDto toUpdateBookDto(Book book);
}
