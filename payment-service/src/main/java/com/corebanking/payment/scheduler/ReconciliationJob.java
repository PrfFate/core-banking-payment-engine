package com.corebanking.payment.scheduler;

import com.corebanking.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ReconciliationJob {

    private final PaymentRepository paymentRepository;

    public ReconciliationJob(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Run every 1 minute
    @Scheduled(fixedDelayString = "${reconciliation.job.fixed-delay:60000}")
    @Transactional
    public void reconcileStuckPayments() {
        int thresholdMinutes = 1;
        paymentRepository.reconcileStuckPayments(thresholdMinutes);
    }
}
