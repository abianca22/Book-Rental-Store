package org.booksrental.rentalservice.model.mapper;



import org.booksrental.rentalservice.model.dto.CreateRentalDTO;
import org.booksrental.rentalservice.model.dto.UpdateRentalDTO;
import org.booksrental.rentalservice.model.entity.Rental;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "bookId", source = "bookId")
    @Mapping(target = "clientId", source = "clientId")
    Rental toRental(CreateRentalDTO createRentalDTO);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "bookId", source = "bookId")
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "pendingRequest", source = "pendingRequest")
    @Mapping(target = "totalPrice", source = "totalPrice")
    Rental toRental(UpdateRentalDTO updateRentalDTO);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "bookId", source = "bookId")
    @Mapping(target = "clientId", source = "clientId")
    CreateRentalDTO toCreateRentalDTO(Rental rental);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "bookId", source = "bookId")
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "pendingRequest", source = "pendingRequest")
    @Mapping(target = "totalPrice", source = "totalPrice")
    UpdateRentalDTO toUpdateRentalDTO(Rental rental);
}

