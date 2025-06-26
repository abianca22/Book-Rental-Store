package org.booksrental.bookservice.model.mapper;

import org.booksrental.bookservice.model.dto.CreateBookDTO;
import org.booksrental.bookservice.model.dto.UpdateBookDTO;
import org.booksrental.bookservice.model.entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toBook(CreateBookDTO createBookDto);
    Book toBook(UpdateBookDTO updateBookDto);
    UpdateBookDTO toUpdateBookDto(Book book);
}
