package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
    int id,
    String orderCode,
    BigDecimal amount,
    PaymentStatus paymentStatus,
    PaymentMethod paymentMethod,
    String transactionId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum PaymentStatus {
        INITIATED, PROCESSING, PAID, FAILED, REFUNDED;
        
        public static PaymentStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "INITIATED" -> INITIATED;
                case "PROCESSING" -> PROCESSING;
                case "PAID" -> PAID;
                case "FAILED" -> FAILED;
                case "REFUNDED" -> REFUNDED;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, CASH, MOBILE_PAYMENT, ONLINE_BANKING;
        
        public static PaymentMethod fromString(String method) {
            return switch (method.toUpperCase()) {
                case "CREDIT_CARD" -> CREDIT_CARD;
                case "DEBIT_CARD" -> DEBIT_CARD;
                case "CASH" -> CASH;
                case "MOBILE_PAYMENT" -> MOBILE_PAYMENT;
                case "ONLINE_BANKING" -> ONLINE_BANKING;
                default -> throw new IllegalArgumentException("Unknown payment method: " + method);
            };
        }
    }
} 