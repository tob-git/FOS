package com.foodapp.dao;

import com.foodapp.model.Rider;
import com.foodapp.model.Rider.RiderStatus;
import com.foodapp.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RiderDAO {

    private static final String SELECT_ALL_RIDERS = "SELECT * FROM riders ORDER BY id";
    private static final String SELECT_RIDER_BY_ID = "SELECT * FROM riders WHERE id = ?";
    private static final String INSERT_RIDER = "INSERT INTO riders (id, first_name, last_name, phone, email, status, date_of_birth, license_number, license_expiry, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_RIDER = "UPDATE riders SET first_name = ?, last_name = ?, phone = ?, email = ?, status = ?, date_of_birth = ?, license_number = ?, license_expiry = ?, updated_at = ? WHERE id = ?";
    private static final String DELETE_RIDER = "DELETE FROM riders WHERE id = ?";
    private static final String SEARCH_RIDERS = "SELECT * FROM riders WHERE id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY id";
    private static final String SELECT_RIDERS_BY_STATUS = "SELECT * FROM riders WHERE status = ? ORDER BY id";

    private final VehicleDAO vehicleDAO;

    public RiderDAO() {
        this.vehicleDAO = new VehicleDAO();
    }

    public List<Rider> findAll() throws SQLException {
        List<Rider> riders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_RIDERS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Rider rider = mapResultSetToRider(rs);
                riders.add(rider);
            }
        }
        
        return riders;
    }
    
    public Rider findById(String id) throws SQLException {
        Rider rider = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RIDER_BY_ID)) {
            
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rider = mapResultSetToRider(rs);
                    
                    // Get assigned vehicle if exists
                    Vehicle assignedVehicle = vehicleDAO.findByRiderId(id);
                    if (assignedVehicle != null) {
                        rider = new Rider(
                            rider.id(),
                            rider.firstName(),
                            rider.lastName(),
                            rider.phone(),
                            rider.email(),
                            rider.status(),
                            rider.dateOfBirth(),
                            rider.licenseNumber(),
                            rider.licenseExpiry(),
                            assignedVehicle,
                            rider.createdAt(),
                            rider.updatedAt()
                        );
                    }
                }
            }
        }
        
        return rider;
    }
    
    public void insert(Rider rider) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_RIDER)) {
            
            stmt.setString(1, rider.id());
            stmt.setString(2, rider.firstName());
            stmt.setString(3, rider.lastName());
            stmt.setString(4, rider.phone());
            stmt.setString(5, rider.email());
            stmt.setString(6, rider.status().toString());
            stmt.setObject(7, rider.dateOfBirth());
            stmt.setString(8, rider.licenseNumber());
            stmt.setObject(9, rider.licenseExpiry());
            stmt.setObject(10, rider.createdAt());
            stmt.setObject(11, rider.updatedAt());
            
            stmt.executeUpdate();
            
            // If rider has an assigned vehicle, update that vehicle
            if (rider.hasAssignedVehicle()) {
                Vehicle vehicle = rider.assignedVehicle();
                vehicleDAO.update(new Vehicle(
                    vehicle.registrationNumber(),
                    vehicle.type(),
                    vehicle.make(),
                    vehicle.model(),
                    vehicle.yearOfManufacture(),
                    vehicle.color(),
                    vehicle.status(),
                    vehicle.insuranceExpiryDate(),
                    rider.id(),
                    vehicle.createdAt(),
                    LocalDateTime.now()
                ));
            }
        }
    }
    
    public void update(Rider rider) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_RIDER)) {
            
            stmt.setString(1, rider.firstName());
            stmt.setString(2, rider.lastName());
            stmt.setString(3, rider.phone());
            stmt.setString(4, rider.email());
            stmt.setString(5, rider.status().toString());
            stmt.setObject(6, rider.dateOfBirth());
            stmt.setString(7, rider.licenseNumber());
            stmt.setObject(8, rider.licenseExpiry());
            stmt.setObject(9, rider.updatedAt());
            stmt.setString(10, rider.id());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_RIDER)) {
            
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<Rider> search(String searchText) throws SQLException {
        List<Rider> riders = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_RIDERS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rider rider = mapResultSetToRider(rs);
                    riders.add(rider);
                }
            }
        }
        
        return riders;
    }
    
    public List<Rider> findByStatus(RiderStatus status) throws SQLException {
        List<Rider> riders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RIDERS_BY_STATUS)) {
            
            stmt.setString(1, status.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rider rider = mapResultSetToRider(rs);
                    riders.add(rider);
                }
            }
        }
        
        return riders;
    }
    
    public List<Rider> findWithVehicles() throws SQLException {
        List<Rider> riders = findAll();
        
        for (int i = 0; i < riders.size(); i++) {
            Rider rider = riders.get(i);
            Vehicle vehicle = vehicleDAO.findByRiderId(rider.id());
            
            if (vehicle != null) {
                riders.set(i, new Rider(
                    rider.id(),
                    rider.firstName(),
                    rider.lastName(),
                    rider.phone(),
                    rider.email(),
                    rider.status(),
                    rider.dateOfBirth(),
                    rider.licenseNumber(),
                    rider.licenseExpiry(),
                    vehicle,
                    rider.createdAt(),
                    rider.updatedAt()
                ));
            }
        }
        
        return riders;
    }
    
    private Rider mapResultSetToRider(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        RiderStatus status = RiderStatus.fromString(rs.getString("status"));
        LocalDate dateOfBirth = rs.getObject("date_of_birth", LocalDate.class);
        String licenseNumber = rs.getString("license_number");
        LocalDate licenseExpiry = rs.getObject("license_expiry", LocalDate.class);
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new Rider(
            id,
            firstName,
            lastName,
            phone,
            email,
            status,
            dateOfBirth,
            licenseNumber,
            licenseExpiry,
            createdAt,
            updatedAt
        );
    }
} 