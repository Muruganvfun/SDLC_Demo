package com.oms.payment.service;

import com.oms.common.exception.ApiException;
import com.oms.payment.dto.PaymentRequest;
import com.oms.payment.dto.PaymentResponse;
import com.oms.payment.entity.Payment;
import com.oms.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        boolean success = random.nextInt(100) < 90;

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .transactionId(transactionId)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT_CARD")
                .status(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
                .build();

        payment = paymentRepository.save(payment);

        log.info("Payment {} for order {}: {}", 
                payment.getTransactionId(), request.getOrderId(), payment.getStatus());

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrder(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> ApiException.notFound("Payment not found for order: " + orderId));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByTransaction(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> ApiException.notFound("Payment not found: " + transactionId));
        return PaymentResponse.from(payment);
    }
}
