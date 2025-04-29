package com.foodapp.model;

import java.time.LocalDateTime;

public record Customer(
    String username,
    String email,
    String phone,
    String firstName,
    String lastName,
    String password,
    CustomerStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum CustomerStatus {
        ACTIVE, INACTIVE, BLOCKED;
        
        public static CustomerStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "INACTIVE" -> INACTIVE;
                case "BLOCKED" -> BLOCKED;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }


    public String getPhone() {
        return phone;
    }
    public String getUsername() {return username;}
    public CustomerStatus getStatus() {return status;}

    // Utility method for displaying full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    public String getEmail(){return email;}

} 