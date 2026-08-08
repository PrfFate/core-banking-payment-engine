package com.corebanking.account.mapper;

import com.corebanking.account.dto.CustomerDto;
import com.corebanking.account.dto.RegisterRequest;
import com.corebanking.account.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {
    
    CustomerDto toDto(Customer customer);

    @Mapping(target = "password", ignore = true) // Handled in service with encoder
    @Mapping(target = "role", constant = "ROLE_USER")
    Customer toEntity(RegisterRequest request);
}
