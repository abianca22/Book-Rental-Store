package org.booksrental.userservice.model.mapper;
import org.booksrental.userservice.model.dto.CreateUserDTO;
import org.booksrental.userservice.model.dto.UpdateUserDTO;
import org.booksrental.userservice.model.dto.ViewUserDTO;
import org.booksrental.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", ignore = true)
    User toUser(CreateUserDTO createUserDto);
    @Mapping(target = "role", ignore = true)
    User toUser(UpdateUserDTO updateUserDto);
    @Mapping(target = "role", ignore = true)
    User toUser(ViewUserDTO viewUserDto);
    @Mapping(target = "role", ignore = true)
    ViewUserDTO toViewUserDto(User user);
    @Mapping(target = "role", ignore = true)
    UpdateUserDTO toUpdateUserDto(User user);
    @Mapping(target = "role", ignore = true)
    CreateUserDTO toCreateUserDto(User user);
}
