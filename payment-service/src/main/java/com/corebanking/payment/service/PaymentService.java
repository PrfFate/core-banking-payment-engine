package com.corebanking.payment.service;

import com.corebanking.payment.dto.TransferRequest;
import com.corebanking.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentRepository.TransferResult executeTransfer(String idempotencyKey, TransferRequest request) {
        // Oracle Procedure invocation
        PaymentRepository.TransferResult result = paymentRepository.executeTransfer(
                idempotencyKey,
                request.senderId(),
                request.receiverId(),
                request.amount()
        );
        
        return result;
    }
}
