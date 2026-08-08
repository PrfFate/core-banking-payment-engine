package com.corebanking.account.dto;

import com.corebanking.account.common.ErrorCodes;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequest {
    @NotBlank(message = ErrorCodes.VAL_CURRENCY_REQUIRED)
    private String currency; // TRY, USD, EUR
}
