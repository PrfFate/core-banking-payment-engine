package com.corebanking.fastmock.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class FastPaymentListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Random random = new Random();

    public FastPaymentListener(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "fast-payments", groupId = "fast-mock-group")
    public void processPaymentEvent(String payload) {
        int chance = random.nextInt(100) + 1; // 1 to 100

        // Parse basic JSON manually or just construct a new JSON string to simulate response
        // In a real scenario, use Jackson ObjectMapper
        // For simulation, we just need to return the same payload wrapped with a status

        String resultPayload = "";
        String status = "";

        if (chance <= 70) {
            status = "SUCCESS";
            log.info("Outcome: SUCCESS (Chance: {})", chance);
        } else if (chance <= 80) {
            status = "REJECT";
            log.info("Outcome: REJECT (Chance: {})", chance);
        } else {
            log.info("Outcome: TIMEOUT (Chance: {}). Acting as a black hole, no response will be sent.", chance);
            return; // Simulate TIMEOUT by not sending any response back
        }

        resultPayload = String.format("{\"status\":\"%s\",\"originalPayload\":%s}", status, payload);

        // Send to result topic
        kafkaTemplate.send("fast-payments-result", resultPayload);
    }
}
