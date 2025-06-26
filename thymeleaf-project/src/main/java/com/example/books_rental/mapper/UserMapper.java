package com.example.books_rental.mapper;

import com.example.books_rental.dto.CreateUserDto;
import com.example.books_rental.dto.UpdateUserDto;
import com.example.books_rental.model.entities.User;
import jakarta.validation.Valid;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserDto createUserDto);
    User toUser(UpdateUserDto updateUserDto);
    UpdateUserDto toUpdateUserDto(User user);
}
