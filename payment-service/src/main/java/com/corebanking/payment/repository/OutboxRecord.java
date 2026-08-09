package com.corebanking.payment.repository;

public record OutboxRecord(
        Long id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload
) {
}
