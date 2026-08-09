package com.corebanking.payment.controller;

import com.corebanking.payment.dto.TransferRequest;
import com.corebanking.payment.repository.PaymentRepository;
import com.corebanking.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> initiateTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Identity-Number", required = false) String identityNumber,
            @RequestBody TransferRequest request) {

        // In a real application we would validate if identityNumber owns the senderId.
        
        PaymentRepository.TransferResult result = paymentService.executeTransfer(idempotencyKey, request);
        
        if ("SUCCESS".equals(result.resultCode())) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
