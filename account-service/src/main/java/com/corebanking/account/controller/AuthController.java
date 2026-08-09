package com.corebanking.account.controller;

import com.corebanking.account.common.ApiResponse;
import com.corebanking.account.dto.CustomerDto;
import com.corebanking.account.dto.JwtResponse;
import com.corebanking.account.dto.LoginRequest;
import com.corebanking.account.dto.RegisterRequest;
import com.corebanking.account.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(jwtResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerDto>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        CustomerDto customerDto = authService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success(customerDto, "User registered successfully"));
    }
}
