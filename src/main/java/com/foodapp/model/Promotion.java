package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Promotion(
    String code,
    String description,
    PromotionType type,
    BigDecimal value,
    LocalDateTime startDate,
    LocalDateTime endDate,
    PromotionStatus status,
    String restaurantSlug,
    int usageLimit,
    int usageCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum PromotionType {
        PERCENTAGE, FIXED_AMOUNT;
        
        public static PromotionType fromString(String type) {
            return switch (type.toUpperCase()) {
                case "PERCENTAGE" -> PERCENTAGE;
                case "FIXED_AMOUNT" -> FIXED_AMOUNT;
                default -> throw new IllegalArgumentException("Unknown promotion type: " + type);
            };
        }
    }
    
    public enum PromotionStatus {
        ACTIVE, INACTIVE, EXPIRED;
        
        public static PromotionStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "INACTIVE" -> INACTIVE;
                case "EXPIRED" -> EXPIRED;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == PromotionStatus.ACTIVE 
                && now.isAfter(startDate) 
                && now.isBefore(endDate)
                && (usageLimit == 0 || usageCount < usageLimit);
    }
    
    public String getFormattedValue() {
        if (type == PromotionType.PERCENTAGE) {
            return value + "%";
        } else {
            return "$" + value;
        }
    }
    
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (type == PromotionType.PERCENTAGE) {
            return subtotal.multiply(value.divide(BigDecimal.valueOf(100)));
        } else {
            return value.min(subtotal); // Ensure discount doesn't exceed subtotal
        }
    }
} 