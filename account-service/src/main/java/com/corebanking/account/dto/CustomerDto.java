package com.corebanking.account.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private Long id;
    private String identityNumber;
    private String fullName;
    private String role;
}
