package com.corebanking.account.controller;

import com.corebanking.account.common.ApiResponse;
import com.corebanking.account.dto.AccountDto;
import com.corebanking.account.dto.CreateAccountRequest;
import com.corebanking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts(Authentication authentication) {
        // authentication.getName() returns the identityNumber from JWT
        List<AccountDto> accounts = accountService.getCustomerAccounts(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        
        AccountDto createdAccount = accountService.createAccount(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(createdAccount, "Account created successfully"));
    }
}
