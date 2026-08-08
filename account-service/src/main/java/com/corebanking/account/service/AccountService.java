package com.corebanking.account.service;

import com.corebanking.account.common.ErrorCodes;
import com.corebanking.account.dto.AccountDto;
import com.corebanking.account.dto.CreateAccountRequest;
import com.corebanking.account.mapper.AccountMapper;
import com.corebanking.account.model.Account;
import com.corebanking.account.model.Customer;
import com.corebanking.account.repository.AccountRepository;
import com.corebanking.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public List<AccountDto> getCustomerAccounts(String identityNumber) {
        List<Account> accounts = accountRepository.findByCustomer_IdentityNumber(identityNumber);
        return accountMapper.toDtoList(accounts);
    }

    @Transactional
    public AccountDto createAccount(String identityNumber, CreateAccountRequest request) {
        Customer customer = customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCodes.AUTH_USER_NOT_FOUND + "|Customer not found"));

        Account newAccount = Account.builder()
                .customer(customer)
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency().toUpperCase())
                .status("ACTIVE")
                .build();

        newAccount = accountRepository.save(newAccount);
        return accountMapper.toDto(newAccount);
    }
}
