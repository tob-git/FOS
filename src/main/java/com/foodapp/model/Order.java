package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
    String orderCode,
    String customerUsername,
    String restaurantSlug,
    OrderStatus status,
    BigDecimal totalAmount,
    BigDecimal discountAmount,
    String deliveryAddress,
    String specialInstructions,
    String riderId,
    String promoCode,
    List<OrderItem> orderItems,
    LocalDateTime placedAt,
    LocalDateTime updatedAt
) {
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, IN_TRANSIT, DELIVERED, CANCELLED;
        
        public static OrderStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "PENDING" -> PENDING;
                case "CONFIRMED" -> CONFIRMED;
                case "PREPARING" -> PREPARING;
                case "READY_FOR_PICKUP" -> READY_FOR_PICKUP;
                case "IN_TRANSIT" -> IN_TRANSIT;
                case "DELIVERED" -> DELIVERED;
                case "CANCELLED" -> CANCELLED;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    // Constructor overload for creating an order without items
    public Order(
            String orderCode,
            String customerUsername,
            String restaurantSlug,
            OrderStatus status,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            String deliveryAddress,
            String specialInstructions,
            String riderId,
            String promoCode,
            LocalDateTime placedAt,
            LocalDateTime updatedAt) {
        this(orderCode, customerUsername, restaurantSlug, status, totalAmount, discountAmount, 
             deliveryAddress, specialInstructions, riderId, promoCode, null, placedAt, updatedAt);
    }
    
    public BigDecimal getSubtotal() {
        return totalAmount.add(discountAmount);
    }
    
    public String getFormattedTotal() {
        return "$" + totalAmount.toString();
    }
} 