package com.example.books_rental.mapper;

import com.example.books_rental.dto.UpdateRentalDto;
import com.example.books_rental.model.entities.Rental;
import org.mapstruct.Mapper;
import com.example.books_rental.dto.CreateRentalDto;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    Rental toRental(CreateRentalDto createRentalDto);
    Rental toRental(UpdateRentalDto updateRentalDto);
}
