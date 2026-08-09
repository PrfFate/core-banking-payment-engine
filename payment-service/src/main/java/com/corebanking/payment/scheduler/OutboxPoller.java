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
    public void pollOutbox() throws Exception {
        List<OutboxRecord> records = outboxRepository.fetchUnprocessedRecords();
        
        if (records.isEmpty()) {
            return;
        }

        for (OutboxRecord record : records) {
            kafkaTemplate.send("fast-payments", record.aggregateId(), record.payload()).get();
        }

        // Mark as processed
        List<Long> processedIds = records.stream().map(OutboxRecord::id).toList();
        outboxRepository.markAsProcessed(processedIds);
    }
}
