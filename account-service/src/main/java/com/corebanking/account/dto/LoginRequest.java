package com.corebanking.account.dto;

import com.corebanking.account.validation.ValidTckn;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import com.corebanking.account.common.ErrorCodes;

@Data
public class LoginRequest {
    @NotBlank(message = ErrorCodes.VAL_IDENTITY_REQUIRED)
    @ValidTckn
    private String identityNumber;

    @NotBlank(message = ErrorCodes.VAL_PASSWORD_REQUIRED)
    private String password;
}
