package com.foodapp.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record Restaurant(
    String slug,
    String name,
    String description,
    String logoUrl,
    String phone,
    String email,
    String website,
    RestaurantStatus status,
    LocalTime openingTime,
    LocalTime closingTime,
    Address address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum RestaurantStatus {
        OPEN, CLOSED, TEMPORARILY_UNAVAILABLE;
        
        public static RestaurantStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "OPEN" -> OPEN;
                case "CLOSED" -> CLOSED;
                case "TEMPORARILY_UNAVAILABLE" -> TEMPORARILY_UNAVAILABLE;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    public boolean isCurrentlyOpen() {
        LocalTime now = LocalTime.now();
        return status == RestaurantStatus.OPEN && 
               !now.isBefore(openingTime) && 
               !now.isAfter(closingTime);
    }
} 