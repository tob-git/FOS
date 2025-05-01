package com.foodapp.dao;

import com.foodapp.model.Address;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {
    
    private static final String SELECT_BY_CUSTOMER = 
            "SELECT id, street, city, state, postal_code, country, latitude, longitude, customer_username " +
            "FROM addresses WHERE customer_username = ?";
    
    private static final String INSERT_ADDRESS = 
            "INSERT INTO addresses (street, city, state, postal_code, country, latitude, longitude, customer_username) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_ADDRESS = 
            "UPDATE addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ?, " +
            "latitude = ?, longitude = ? WHERE id = ?";
    
    private static final String DELETE_ADDRESS = 
            "DELETE FROM addresses WHERE id = ?";
    
    private static final String DELETE_CUSTOMER_ADDRESSES = 
            "DELETE FROM addresses WHERE customer_username = ?";
    
    public List<Address> findByCustomer(String customerUsername) throws SQLException {
        List<Address> addresses = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CUSTOMER)) {
            
            stmt.setString(1, customerUsername);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRowToAddress(rs));
                }
            }
        }
        
        return addresses;
    }
    
    public Address insert(Address address) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ADDRESS, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, address.street());
            stmt.setString(2, address.city());
            stmt.setString(3, address.state());
            stmt.setString(4, address.postalCode());
            stmt.setString(5, address.country());
            
            if (address.latitude() != null) {
                stmt.setBigDecimal(6, address.latitude());
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
            }
            
            if (address.longitude() != null) {
                stmt.setBigDecimal(7, address.longitude());
            } else {
                stmt.setNull(7, java.sql.Types.DECIMAL);
            }
            
            stmt.setString(8, address.customerUsername());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating address failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Address(
                        id, 
                        address.street(), 
                        address.city(), 
                        address.state(), 
                        address.postalCode(), 
                        address.country(), 
                        address.latitude(), 
                        address.longitude(), 
                        address.customerUsername()
                    );
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
    }
    
    public void update(Address address) throws SQLException {
        if (address.isNew()) {
            throw new IllegalArgumentException("Cannot update an address without an ID");
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ADDRESS)) {
            
            stmt.setString(1, address.street());
            stmt.setString(2, address.city());
            stmt.setString(3, address.state());
            stmt.setString(4, address.postalCode());
            stmt.setString(5, address.country());
            
            if (address.latitude() != null) {
                stmt.setBigDecimal(6, address.latitude());
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
            }
            
            if (address.longitude() != null) {
                stmt.setBigDecimal(7, address.longitude());
            } else {
                stmt.setNull(7, java.sql.Types.DECIMAL);
            }
            
            stmt.setInt(8, address.id());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(int addressId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_ADDRESS)) {
            
            stmt.setInt(1, addressId);
            stmt.executeUpdate();
        }
    }
    
    public void deleteCustomerAddresses(String customerUsername) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_CUSTOMER_ADDRESSES)) {
            
            stmt.setString(1, customerUsername);
            stmt.executeUpdate();
        }
    }
    
    private Address mapRowToAddress(ResultSet rs) throws SQLException {
        return new Address(
            rs.getInt("id"),
            rs.getString("street"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("postal_code"),
            rs.getString("country"),
            rs.getBigDecimal("latitude"),
            rs.getBigDecimal("longitude"),
            rs.getString("customer_username")
        );
    }
} 