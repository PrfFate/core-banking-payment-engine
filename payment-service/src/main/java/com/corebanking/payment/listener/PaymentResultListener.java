package com.corebanking.payment.listener;

import com.corebanking.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentResultListener {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    public PaymentResultListener(PaymentRepository paymentRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "fast-payments-result", groupId = "payment-saga-group")
    public void processPaymentResult(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String status = root.path("status").asText();
        
        JsonNode original = root.path("originalPayload");
        Long transactionId = original.path("transactionId").asLong();

        if (transactionId == 0 || status.isEmpty()) {
            throw new IllegalArgumentException("Invalid result payload format: " + payload);
        }

        String dbResult = paymentRepository.processPaymentResult(transactionId, status);
        
        if ("IGNORED_OR_NOT_FOUND".equals(dbResult)) {
            // It was already processed or compensated, this is fine, but we can log at debug if needed
            log.debug("Transaction {} was not in PENDING state. Guard condition prevented double processing.", transactionId);
        }
    }
}
