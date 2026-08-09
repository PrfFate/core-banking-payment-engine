package com.corebanking.payment.dto;

public record TransferRequest(
        Long senderId,
        Long receiverId,
        java.math.BigDecimal amount
) {
}
