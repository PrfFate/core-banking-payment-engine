package com.corebanking.account.service;

import com.corebanking.account.common.ErrorCodes;
import com.corebanking.account.dto.AccountDto;
import com.corebanking.account.dto.CreateAccountRequest;
import com.corebanking.account.mapper.AccountMapper;
import com.corebanking.account.model.Account;
import com.corebanking.account.model.Customer;
import com.corebanking.account.repository.AccountRepository;
import com.corebanking.account.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Customer mockCustomer;
    private Account mockAccount;
    private AccountDto mockAccountDto;

    @BeforeEach
    void setUp() {
        mockCustomer = Customer.builder()
                .id(1L)
                .identityNumber("12345678901")
                .fullName("John Doe")
                .build();

        mockAccount = Account.builder()
                .id(100L)
                .customer(mockCustomer)
                .balance(BigDecimal.ZERO)
                .currency("TRY")
                .status("ACTIVE")
                .build();

        mockAccountDto = new AccountDto();
        mockAccountDto.setId(100L);
        mockAccountDto.setBalance(BigDecimal.ZERO);
        mockAccountDto.setCurrency("TRY");
        mockAccountDto.setStatus("ACTIVE");
    }

    @Test
    void getCustomerAccounts_ShouldReturnAccountList() {
        // Arrange
        when(accountRepository.findByCustomer_IdentityNumber("12345678901")).thenReturn(List.of(mockAccount));
        when(accountMapper.toDtoList(anyList())).thenReturn(List.of(mockAccountDto));

        // Act
        List<AccountDto> result = accountService.getCustomerAccounts("12345678901");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TRY", result.get(0).getCurrency());
        verify(accountRepository).findByCustomer_IdentityNumber("12345678901");
    }

    @Test
    void createAccount_ShouldCreateSuccessfully_WhenCustomerExists() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrency("USD");

        when(customerRepository.findByIdentityNumber("12345678901")).thenReturn(Optional.of(mockCustomer));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(accountMapper.toDto(any(Account.class))).thenReturn(mockAccountDto);

        // Act
        AccountDto result = accountService.createAccount("12345678901", request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowException_WhenCustomerNotFound() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrency("USD");

        when(customerRepository.findByIdentityNumber("99999999999")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount("99999999999", request);
        });

        assertTrue(exception.getMessage().contains(ErrorCodes.AUTH_USER_NOT_FOUND));
        verify(accountRepository, never()).save(any());
    }
}
