package com.foodapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record Customer(
    String username,
    String email,
    String phone,
    String firstName,
    String lastName,
    String password,
    CustomerStatus status,
    List<Address> addresses,
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

    // Constructor without addresses for backward compatibility
    public Customer(
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
        this(username, email, phone, firstName, lastName, password, status, new ArrayList<>(), createdAt, updatedAt);
    }

    public String getPhone() {
        return phone;
    }
    
    public String getUsername() {
        return username;
    }
    
    public CustomerStatus getStatus() {
        return status;
    }

    // Utility method for displaying full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    // Get default address (first in the list) or null if none exists
    public Address getDefaultAddress() {
        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
    }
    
    // Add an address to this customer
    public Customer withAddress(Address address) {
        List<Address> newAddresses = new ArrayList<>(addresses);
        newAddresses.add(address);
        return new Customer(
            username, email, phone, firstName, lastName, password, 
            status, newAddresses, createdAt, updatedAt
        );
    }
} 