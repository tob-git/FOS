package com.foodapp.dao;

import com.foodapp.model.Address;
import com.foodapp.model.Restaurant;
import com.foodapp.model.Restaurant.RestaurantStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {

    private static final String SELECT_ALL_RESTAURANTS = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id ORDER BY r.name";
    private static final String SELECT_RESTAURANT_BY_SLUG = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id WHERE r.slug = ?";
    private static final String INSERT_ADDRESS = "INSERT INTO addresses (street, city, state, postal_code, country, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_RESTAURANT = "INSERT INTO restaurants (slug, name, description, logo_url, phone, email, website, status, opening_time, closing_time, address_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ADDRESS = "UPDATE addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ?, latitude = ?, longitude = ? WHERE id = ?";
    private static final String UPDATE_RESTAURANT = "UPDATE restaurants SET name = ?, description = ?, logo_url = ?, phone = ?, email = ?, website = ?, status = ?, opening_time = ?, closing_time = ?, updated_at = ? WHERE slug = ?";
    private static final String DELETE_RESTAURANT = "DELETE FROM restaurants WHERE slug = ?";
    private static final String SEARCH_RESTAURANTS = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id WHERE r.name LIKE ? OR r.description LIKE ? OR a.city LIKE ? ORDER BY r.name";

    public List<Restaurant> findAll() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_RESTAURANTS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Restaurant restaurant = mapResultSetToRestaurant(rs);
                restaurants.add(restaurant);
            }
        }
        
        return restaurants;
    }
    
    public Restaurant findBySlug(String slug) throws SQLException {
        Restaurant restaurant = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RESTAURANT_BY_SLUG)) {
            
            stmt.setString(1, slug);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    restaurant = mapResultSetToRestaurant(rs);
                }
            }
        }
        
        return restaurant;
    }
    
    public void insert(Restaurant restaurant) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // First insert the address and get its ID
            int addressId;
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_ADDRESS, PreparedStatement.RETURN_GENERATED_KEYS)) {
                Address address = restaurant.address();
                
                stmt.setString(1, address.street());
                stmt.setString(2, address.city());
                stmt.setString(3, address.state());
                stmt.setString(4, address.postalCode());
                stmt.setString(5, address.country());
                stmt.setDouble(6, address.latitude());
                stmt.setDouble(7, address.longitude());
                
                stmt.executeUpdate();
                
                // Get the generated address ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        addressId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating address failed, no ID obtained.");
                    }
                }
            }
            
            // Then insert the restaurant with the address ID
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_RESTAURANT)) {
                stmt.setString(1, restaurant.slug());
                stmt.setString(2, restaurant.name());
                stmt.setString(3, restaurant.description());
                stmt.setString(4, restaurant.logoUrl());
                stmt.setString(5, restaurant.phone());
                stmt.setString(6, restaurant.email());
                stmt.setString(7, restaurant.website());
                stmt.setString(8, restaurant.status().toString());
                stmt.setObject(9, restaurant.openingTime());
                stmt.setObject(10, restaurant.closingTime());
                stmt.setInt(11, addressId);
                stmt.setObject(12, restaurant.createdAt());
                stmt.setObject(13, restaurant.updatedAt());
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    public void update(Restaurant restaurant) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // First update the address
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_ADDRESS)) {
                Address address = restaurant.address();
                int addressId = address.id();
                
                stmt.setString(1, address.street());
                stmt.setString(2, address.city());
                stmt.setString(3, address.state());
                stmt.setString(4, address.postalCode());
                stmt.setString(5, address.country());
                stmt.setDouble(6, address.latitude());
                stmt.setDouble(7, address.longitude());
                stmt.setInt(8, addressId);
                
                stmt.executeUpdate();
            }
            
            // Then update the restaurant
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_RESTAURANT)) {
                stmt.setString(1, restaurant.name());
                stmt.setString(2, restaurant.description());
                stmt.setString(3, restaurant.logoUrl());
                stmt.setString(4, restaurant.phone());
                stmt.setString(5, restaurant.email());
                stmt.setString(6, restaurant.website());
                stmt.setString(7, restaurant.status().toString());
                stmt.setObject(8, restaurant.openingTime());
                stmt.setObject(9, restaurant.closingTime());
                stmt.setObject(10, restaurant.updatedAt());
                stmt.setString(11, restaurant.slug());
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    public void delete(String slug) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_RESTAURANT)) {
            
            stmt.setString(1, slug);
            stmt.executeUpdate();
        }
    }
    
    public List<Restaurant> search(String searchText) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_RESTAURANTS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    restaurants.add(restaurant);
                }
            }
        }
        
        return restaurants;
    }
    
    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        // Map the address
        int addressId = rs.getInt("a.id");
        String street = rs.getString("a.street");
        String city = rs.getString("a.city");
        String state = rs.getString("a.state");
        String postalCode = rs.getString("a.postal_code");
        String country = rs.getString("a.country");
        double latitude = rs.getDouble("a.latitude");
        double longitude = rs.getDouble("a.longitude");
        
        Address address = new Address(
            addressId,
            street,
            city,
            state,
            postalCode,
            country,
            latitude,
            longitude,
            null
        );
        
        // Map the restaurant
        String slug = rs.getString("r.slug");
        String name = rs.getString("r.name");
        String description = rs.getString("r.description");
        String logoUrl = rs.getString("r.logo_url");
        String phone = rs.getString("r.phone");
        String email = rs.getString("r.email");
        String website = rs.getString("r.website");
        RestaurantStatus status = RestaurantStatus.fromString(rs.getString("r.status"));
        LocalTime openingTime = rs.getObject("r.opening_time", LocalTime.class);
        LocalTime closingTime = rs.getObject("r.closing_time", LocalTime.class);
        LocalDateTime createdAt = rs.getObject("r.created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("r.updated_at", LocalDateTime.class);
        
        return new Restaurant(
            slug,
            name,
            description,
            logoUrl,
            phone,
            email,
            website,
            status,
            openingTime,
            closingTime,
            address,
            createdAt,
            updatedAt
        );
    }
} 