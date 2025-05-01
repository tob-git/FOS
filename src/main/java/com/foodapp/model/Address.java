package com.foodapp.model;

import java.math.BigDecimal;

public record Address(
    int id,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    BigDecimal latitude,
    BigDecimal longitude,
    String customerUsername
) {
    // Constructor without ID for new addresses
    public Address(
        String street,
        String city,
        String state,
        String postalCode,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String customerUsername
    ) {
        this(0, street, city, state, postalCode, country, latitude, longitude, customerUsername);
    }
    
    // Constructor with default country and no coordinates
    public Address(
        String street,
        String city,
        String state,
        String postalCode,
        String customerUsername
    ) {
        this(0, street, city, state, postalCode, "United States", null, null, customerUsername);
    }
    
    // Get full address as a formatted string
    public String getFullAddress() {
        return street + ", " + city + ", " + state + " " + postalCode + ", " + country;
    }
    
    // Check if this is a new address (not saved to database yet)
    public boolean isNew() {
        return id == 0;
    }
} 