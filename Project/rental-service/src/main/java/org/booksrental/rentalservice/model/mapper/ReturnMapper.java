package org.booksrental.rentalservice.model.mapper;

import org.booksrental.rentalservice.model.dto.ReturnDTO;
import org.booksrental.rentalservice.model.entity.Return;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReturnMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "employeeId", source = "employeeId")
    Return toReturn(ReturnDTO returnDTO);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "employeeId", source = "employeeId")
    ReturnDTO toReturnDTO(Return rentalReturn);
}

