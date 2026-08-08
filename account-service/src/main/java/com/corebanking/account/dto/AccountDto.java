package com.corebanking.account.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private Long id;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
