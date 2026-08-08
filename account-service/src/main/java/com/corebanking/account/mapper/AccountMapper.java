package com.corebanking.account.mapper;

import com.corebanking.account.dto.AccountDto;
import com.corebanking.account.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    
    AccountDto toDto(Account account);
    
    List<AccountDto> toDtoList(List<Account> accounts);
}
