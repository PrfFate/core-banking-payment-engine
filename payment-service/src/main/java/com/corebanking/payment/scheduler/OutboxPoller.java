package com.corebanking.payment.scheduler;

import com.corebanking.payment.repository.OutboxRecord;
import com.corebanking.payment.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPoller(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay:1000}")
    @Transactional
    public void pollOutbox() {
        log.debug("Polling outbox table for unprocessed records...");
        
        List<OutboxRecord> records = outboxRepository.fetchUnprocessedRecords();
        
        if (records.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox records.", records.size());

        for (OutboxRecord record : records) {
            try {
                // Send to Kafka fast-payments topic
                kafkaTemplate.send("fast-payments", record.aggregateId(), record.payload()).get();
                log.info("Successfully sent outbox event {} to Kafka.", record.id());
            } catch (Exception e) {
                log.error("Failed to send outbox event {} to Kafka. Reason: {}", record.id(), e.getMessage());
                // Throwing exception ensures the transaction rolls back, 
                // so the records remain processed=0 in the DB.
                throw new RuntimeException("Kafka send failed", e);
            }
        }

        // Mark as processed
        List<Long> processedIds = records.stream().map(OutboxRecord::id).toList();
        outboxRepository.markAsProcessed(processedIds);
        
        log.info("Marked {} outbox records as processed.", processedIds.size());
    }
}
