package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public enum PromotionType {
        PERCENTAGE, FIXED_AMOUNT;
        
        public static PromotionType fromString(String type) {
            try {
                return valueOf(type);
            } catch (IllegalArgumentException e) {
                return FIXED_AMOUNT; // Default value
            }
        }
    }
    
    public enum PromotionStatus {
        ACTIVE, INACTIVE, EXPIRED;
        
        public static PromotionStatus fromString(String status) {
            try {
                return valueOf(status);
            } catch (IllegalArgumentException e) {
                return INACTIVE; // Default value
            }
        }
    }
    
    public String getFormattedDiscount() {
        if (type == PromotionType.PERCENTAGE) {
            return value + "%";
        } else {
            return "$" + value;
        }
    }
    
    public String getFormattedStartDate() {
        return startDate != null ? startDate.format(DATE_FORMATTER) : "";
    }
    
    public String getFormattedEndDate() {
        return endDate != null ? endDate.format(DATE_FORMATTER) : "";
    }
    
    public String getFormattedStatus() {
        return status != null ? status.toString() : "";
    }
    
    public String getUsageInfo() {
        if (usageLimit <= 0) {
            return usageCount + " / Unlimited";
        }
        return usageCount + " / " + usageLimit;
    }
    
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (type == PromotionType.PERCENTAGE) {
            // Calculate percentage discount and ensure it doesn't exceed subtotal
            BigDecimal discount = subtotal.multiply(value.divide(new BigDecimal("100")));
            return discount.min(subtotal);
        } else {
            // Fixed amount discount should not exceed subtotal
            return value.min(subtotal);
        }
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == PromotionStatus.ACTIVE && 
               now.isAfter(startDate) && 
               now.isBefore(endDate) &&
               (usageLimit <= 0 || usageCount < usageLimit);
    }
} 