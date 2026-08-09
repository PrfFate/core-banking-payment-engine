package com.corebanking.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public CommonErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        // Publish to <original_topic>.DLT (or customized) on failure
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template, 
            (r, e) -> new org.apache.kafka.common.TopicPartition(r.topic() + "-dlq", r.partition())
        );
        
        // Retry 3 times with 1-second delay between attempts
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }
}
