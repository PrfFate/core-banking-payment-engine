package com.corebanking.account.service;

import com.corebanking.account.common.ErrorCodes;
import com.corebanking.account.dto.CustomerDto;
import com.corebanking.account.dto.RegisterRequest;
import com.corebanking.account.mapper.CustomerMapper;
import com.corebanking.account.model.Customer;
import com.corebanking.account.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ShouldCreateUser_WhenIdentityIsUnique() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setIdentityNumber("12345678901");
        request.setPassword("password123");
        request.setFullName("John Doe");

        Customer mappedCustomer = Customer.builder().build();
        Customer savedCustomer = Customer.builder().id(1L).build();
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(1L);

        when(customerRepository.findByIdentityNumber("12345678901")).thenReturn(Optional.empty());
        when(customerMapper.toEntity(request)).thenReturn(mappedCustomer);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerMapper.toDto(savedCustomer)).thenReturn(customerDto);

        // Act
        CustomerDto result = authService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).save(any(Customer.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerUser_ShouldThrowException_WhenIdentityExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setIdentityNumber("12345678901");

        when(customerRepository.findByIdentityNumber("12345678901"))
                .thenReturn(Optional.of(Customer.builder().build()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(request);
        });

        assertTrue(exception.getMessage().contains(ErrorCodes.AUTH_USER_EXISTS));
        verify(customerRepository, never()).save(any());
    }
}
