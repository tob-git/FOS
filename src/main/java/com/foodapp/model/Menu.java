package com.foodapp.model;

import java.time.LocalDateTime;

public record Menu(
    int id,
    String name,
    String description,
    MenuStatus status,
    String restaurantSlug,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum MenuStatus {
        ACTIVE, INACTIVE, SEASONAL;
        
        public static MenuStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "INACTIVE" -> INACTIVE;
                case "SEASONAL" -> SEASONAL;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
} 