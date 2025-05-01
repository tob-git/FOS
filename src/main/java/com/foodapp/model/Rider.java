package com.foodapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Rider(
    String id,
    String firstName,
    String lastName,
    String phone,
    String email,
    RiderStatus status,
    LocalDate dateOfBirth,
    String licenseNumber,
    LocalDate licenseExpiry,
    Vehicle assignedVehicle,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum RiderStatus {
        ACTIVE, INACTIVE, ON_DELIVERY, ON_BREAK;
        
        public static RiderStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "INACTIVE" -> INACTIVE;
                case "ON_DELIVERY" -> ON_DELIVERY;
                case "ON_BREAK" -> ON_BREAK;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    // Constructor overload for creating a rider without an assigned vehicle
    public Rider(
            String id,
            String firstName,
            String lastName,
            String phone,
            String email,
            RiderStatus status,
            LocalDate dateOfBirth,
            String licenseNumber,
            LocalDate licenseExpiry,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this(id, firstName, lastName, phone, email, status, 
             dateOfBirth, licenseNumber, licenseExpiry, null, createdAt, updatedAt);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean hasAssignedVehicle() {
        System.out.println("Checking if rider has assigned vehicle: " + assignedVehicle);
        return assignedVehicle != null;
    }
} 