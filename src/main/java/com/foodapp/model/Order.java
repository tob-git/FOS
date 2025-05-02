package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
    String orderCode,
    String customerUsername,
    String restaurantSlug,
    OrderStatus status,
    BigDecimal discountAmount,
    int addressId,
    String specialInstructions,
    String riderId,
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
            BigDecimal discountAmount,
            int addressId,
            String specialInstructions,
            String riderId,
            LocalDateTime placedAt,
            LocalDateTime updatedAt) {
        this(orderCode, customerUsername, restaurantSlug, status, discountAmount, 
             addressId, specialInstructions, riderId, null, placedAt, updatedAt);
    }
    
    /**
     * Calculates the subtotal (sum of all items)
     * @return Subtotal amount
     */
    public BigDecimal getSubtotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate from order items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.priceAtOrderTime().multiply(new BigDecimal(item.quantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }
    
    /**
     * Calculates the total amount (subtotal - discount)
     * @return Total amount
     */
    public BigDecimal getTotalAmount() {
        BigDecimal subtotal = getSubtotal();
        return discountAmount != null ? subtotal.subtract(discountAmount) : subtotal;
    }
    
    /**
     * Gets total amount with proper formatting
     * @return Formatted total amount
     */
    public String getFormattedTotal() {
        BigDecimal total = getTotalAmount();
        return "$" + total.toString();
    }
} 