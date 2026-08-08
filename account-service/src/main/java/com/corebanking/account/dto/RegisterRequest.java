package com.corebanking.account.dto;

import com.corebanking.account.validation.ValidTckn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.corebanking.account.common.ErrorCodes;

@Data
public class RegisterRequest {
    @NotBlank(message = ErrorCodes.VAL_IDENTITY_REQUIRED)
    @ValidTckn
    private String identityNumber;

    @NotBlank(message = ErrorCodes.VAL_PASSWORD_REQUIRED)
    @Size(min = 6, message = ErrorCodes.VAL_PASSWORD_MIN_LENGTH)
    private String password;

    @NotBlank(message = ErrorCodes.VAL_FULLNAME_REQUIRED)
    private String fullName;
}
