package com.foodapp.dao;

import com.foodapp.model.Vehicle;
import com.foodapp.model.Vehicle.VehicleStatus;
import com.foodapp.model.Vehicle.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    private static final String SELECT_ALL_VEHICLES = "SELECT * FROM vehicles ORDER BY registration_number";
    private static final String SELECT_VEHICLE_BY_REGISTRATION = "SELECT * FROM vehicles WHERE registration_number = ?";
    private static final String SELECT_VEHICLE_BY_RIDER = "SELECT * FROM vehicles WHERE rider_id = ?";
    private static final String INSERT_VEHICLE = "INSERT INTO vehicles (registration_number, type, make, model, year_of_manufacture, color, status, insurance_expiry_date, rider_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_VEHICLE = "UPDATE vehicles SET type = ?, make = ?, model = ?, year_of_manufacture = ?, color = ?, status = ?, insurance_expiry_date = ?, rider_id = ?, updated_at = ? WHERE registration_number = ?";
    private static final String DELETE_VEHICLE = "DELETE FROM vehicles WHERE registration_number = ?";
    private static final String SEARCH_VEHICLES = "SELECT * FROM vehicles WHERE registration_number LIKE ? OR make LIKE ? OR model LIKE ? OR color LIKE ? ORDER BY registration_number";
    private static final String SELECT_VEHICLES_BY_STATUS = "SELECT * FROM vehicles WHERE status = ? ORDER BY registration_number";
    private static final String SELECT_AVAILABLE_VEHICLES = "SELECT * FROM vehicles WHERE status = 'ACTIVE' AND rider_id IS NULL ORDER BY registration_number";
    private static final String UNASSIGN_VEHICLE = "UPDATE vehicles SET rider_id = NULL, updated_at = ? WHERE registration_number = ?";

    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_VEHICLES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Vehicle vehicle = mapResultSetToVehicle(rs);
                vehicles.add(vehicle);
            }
        }
        
        return vehicles;
    }
    
    public Vehicle findByRegistration(String registrationNumber) throws SQLException {
        Vehicle vehicle = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_VEHICLE_BY_REGISTRATION)) {
            
            stmt.setString(1, registrationNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    vehicle = mapResultSetToVehicle(rs);
                }
            }
        }
        
        return vehicle;
    }
    
    public Vehicle findByRiderId(String riderId) throws SQLException {
        Vehicle vehicle = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_VEHICLE_BY_RIDER)) {
            
            stmt.setString(1, riderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    vehicle = mapResultSetToVehicle(rs);
                }
            }
        }
        
        return vehicle;
    }
    
    public void insert(Vehicle vehicle) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_VEHICLE)) {
            
            stmt.setString(1, vehicle.registrationNumber());
            stmt.setString(2, vehicle.type().toString());
            stmt.setString(3, vehicle.make());
            stmt.setString(4, vehicle.model());
            stmt.setInt(5, vehicle.yearOfManufacture());
            stmt.setString(6, vehicle.color());
            stmt.setString(7, vehicle.status().toString());
            stmt.setObject(8, vehicle.insuranceExpiryDate());
            stmt.setString(9, vehicle.riderId());
            stmt.setObject(10, vehicle.createdAt());
            stmt.setObject(11, vehicle.updatedAt());
            
            stmt.executeUpdate();
        }
    }
    
    public void update(Vehicle vehicle) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_VEHICLE)) {
            
            stmt.setString(1, vehicle.type().toString());
            stmt.setString(2, vehicle.make());
            stmt.setString(3, vehicle.model());
            stmt.setInt(4, vehicle.yearOfManufacture());
            stmt.setString(5, vehicle.color());
            stmt.setString(6, vehicle.status().toString());
            stmt.setObject(7, vehicle.insuranceExpiryDate());
            stmt.setString(8, vehicle.riderId());
            stmt.setObject(9, vehicle.updatedAt());
            stmt.setString(10, vehicle.registrationNumber());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String registrationNumber) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_VEHICLE)) {
            
            stmt.setString(1, registrationNumber);
            stmt.executeUpdate();
        }
    }
    
    public List<Vehicle> search(String searchText) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_VEHICLES)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicle vehicle = mapResultSetToVehicle(rs);
                    vehicles.add(vehicle);
                }
            }
        }
        
        return vehicles;
    }
    
    public List<Vehicle> findByStatus(VehicleStatus status) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_VEHICLES_BY_STATUS)) {
            
            stmt.setString(1, status.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicle vehicle = mapResultSetToVehicle(rs);
                    vehicles.add(vehicle);
                }
            }
        }
        
        return vehicles;
    }
    
    public List<Vehicle> findAvailableVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_AVAILABLE_VEHICLES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Vehicle vehicle = mapResultSetToVehicle(rs);
                vehicles.add(vehicle);
            }
        }
        
        return vehicles;
    }
    
    public void unassignVehicle(String registrationNumber) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UNASSIGN_VEHICLE)) {
            
            stmt.setObject(1, LocalDateTime.now());
            stmt.setString(2, registrationNumber);
            
            stmt.executeUpdate();
        }
    }
    
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        String registrationNumber = rs.getString("registration_number");
        VehicleType type = VehicleType.fromString(rs.getString("type"));
        String make = rs.getString("make");
        String model = rs.getString("model");
        int yearOfManufacture = rs.getInt("year_of_manufacture");
        String color = rs.getString("color");
        VehicleStatus status = VehicleStatus.fromString(rs.getString("status"));
        LocalDate insuranceExpiryDate = rs.getObject("insurance_expiry_date", LocalDate.class);
        String riderId = rs.getString("rider_id");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new Vehicle(
            registrationNumber,
            type,
            make,
            model,
            yearOfManufacture,
            color,
            status,
            insuranceExpiryDate,
            riderId,
            createdAt,
            updatedAt
        );
    }
} 