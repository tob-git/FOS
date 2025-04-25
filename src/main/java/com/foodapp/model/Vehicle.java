package com.foodapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Vehicle(
    String registrationNumber,
    VehicleType type,
    String make,
    String model,
    int yearOfManufacture,
    String color,
    VehicleStatus status,
    LocalDate insuranceExpiryDate,
    String riderId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum VehicleType {
        MOTORCYCLE, BICYCLE, CAR, SCOOTER, VAN;
        
        public static VehicleType fromString(String type) {
            return switch (type.toUpperCase()) {
                case "MOTORCYCLE" -> MOTORCYCLE;
                case "BICYCLE" -> BICYCLE;
                case "CAR" -> CAR;
                case "SCOOTER" -> SCOOTER;
                case "VAN" -> VAN;
                default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
            };
        }
    }
    
    public enum VehicleStatus {
        ACTIVE, MAINTENANCE, INACTIVE;
        
        public static VehicleStatus fromString(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "MAINTENANCE" -> MAINTENANCE;
                case "INACTIVE" -> INACTIVE;
                default -> throw new IllegalArgumentException("Unknown status: " + status);
            };
        }
    }
    
    public String getDescription() {
        return yearOfManufacture + " " + make + " " + model + " (" + color + ")";
    }
    
    public boolean isAssigned() {
        return riderId != null && !riderId.isBlank();
    }
} 