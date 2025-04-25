package com.foodapp.model;

public record Address(
    int id,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    double latitude,
    double longitude,
    String customerUsername
) {
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + postalCode + ", " + country;
    }
} 