package com.corebanking.account.service;

import com.corebanking.account.dto.CustomerDto;
import com.corebanking.account.dto.JwtResponse;
import com.corebanking.account.dto.LoginRequest;
import com.corebanking.account.dto.RegisterRequest;
import com.corebanking.account.mapper.CustomerMapper;
import com.corebanking.account.model.Customer;
import com.corebanking.account.repository.CustomerRepository;
import com.corebanking.account.security.JwtUtils;
import com.corebanking.account.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corebanking.account.common.ErrorCodes;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomerMapper customerMapper;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentityNumber(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication.getName());

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .identityNumber(userDetails.getUsername())
                .role(userDetails.getAuthorities().iterator().next().getAuthority())
                .build();
    }

    @Transactional
    public CustomerDto registerUser(RegisterRequest signUpRequest) {
        if (customerRepository.findByIdentityNumber(signUpRequest.getIdentityNumber()).isPresent()) {
            throw new IllegalArgumentException(ErrorCodes.AUTH_USER_EXISTS + "|Identity number is already in use");
        }

        Customer customer = customerMapper.toEntity(signUpRequest);
        customer.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        customer = customerRepository.save(customer);
        return customerMapper.toDto(customer);
    }
}
