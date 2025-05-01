package com.foodapp.dao;

import com.foodapp.model.Address;
import com.foodapp.model.Customer;
import com.foodapp.model.Customer.CustomerStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {
    
    private static final String SELECT_ALL = 
            "SELECT username, email, phone, first_name, last_name, password, status, created_at, updated_at " +
            "FROM customers";
    
    private static final String SELECT_BY_USERNAME = 
            "SELECT username, email, phone, first_name, last_name, password, status, created_at, updated_at " +
            "FROM customers WHERE username = ?";
    
    private static final String INSERT_CUSTOMER = 
            "INSERT INTO customers (username, email, phone, first_name, last_name, password, status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_CUSTOMER = 
            "UPDATE customers SET email = ?, phone = ?, first_name = ?, last_name = ?, " +
            "password = ?, status = ?, updated_at = ? WHERE username = ?";
    
    private static final String DELETE_CUSTOMER = "DELETE FROM customers WHERE username = ?";
    
    // Search customers by username, email, first_name, or last_name
    private static final String SEARCH_CUSTOMERS = 
            "SELECT username, email, phone, first_name, last_name, password, status, created_at, updated_at " +
            "FROM customers WHERE username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ?";
    
    private final AddressDAO addressDAO;
    
    public CustomerDAO() throws SQLException {
        this.addressDAO = new AddressDAO();
    }
    
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                Customer customer = mapRowToCustomer(rs);
                
                // Load addresses for this customer
                List<Address> addresses = addressDAO.findByCustomer(customer.username());
                
                // Create a new customer with addresses
                if (!addresses.isEmpty()) {
                    customer = new Customer(
                        customer.username(),
                        customer.email(),
                        customer.phone(),
                        customer.firstName(),
                        customer.lastName(),
                        customer.password(),
                        customer.status(),
                        addresses,
                        customer.createdAt(),
                        customer.updatedAt()
                    );
                }
                
                customers.add(customer);
            }
        }
        
        return customers;
    }
    
    public Optional<Customer> findByUsername(String username) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = mapRowToCustomer(rs);
                    
                    // Load addresses for this customer
                    List<Address> addresses = addressDAO.findByCustomer(customer.username());
                    
                    // Create a new customer with addresses
                    if (!addresses.isEmpty()) {
                        customer = new Customer(
                            customer.username(),
                            customer.email(),
                            customer.phone(),
                            customer.firstName(),
                            customer.lastName(),
                            customer.password(),
                            customer.status(),
                            addresses,
                            customer.createdAt(),
                            customer.updatedAt()
                        );
                    }
                    
                    return Optional.of(customer);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public void insert(Customer customer) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Insert customer
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_CUSTOMER)) {
                LocalDateTime now = LocalDateTime.now();
                
                stmt.setString(1, customer.username());
                stmt.setString(2, customer.email());
                stmt.setString(3, customer.phone());
                stmt.setString(4, customer.firstName());
                stmt.setString(5, customer.lastName());
                stmt.setString(6, customer.password());
                stmt.setString(7, customer.status().name());
                stmt.setTimestamp(8, Timestamp.valueOf(customer.createdAt() != null ? customer.createdAt() : now));
                stmt.setTimestamp(9, Timestamp.valueOf(customer.updatedAt() != null ? customer.updatedAt() : now));
                
                stmt.executeUpdate();
            }
            
            // Insert addresses if any
            if (customer.addresses() != null && !customer.addresses().isEmpty()) {
                for (Address address : customer.addresses()) {
                    addressDAO.insert(address);
                }
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
    
    public void update(Customer customer) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Update customer
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_CUSTOMER)) {
                LocalDateTime now = LocalDateTime.now();
                
                stmt.setString(1, customer.email());
                stmt.setString(2, customer.phone());
                stmt.setString(3, customer.firstName());
                stmt.setString(4, customer.lastName());
                stmt.setString(5, customer.password());
                stmt.setString(6, customer.status().name());
                stmt.setTimestamp(7, Timestamp.valueOf(now));
                stmt.setString(8, customer.username());
                
                stmt.executeUpdate();
            }
            
            // We don't update addresses here, addresses are managed separately
            
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
    
    public void delete(String username) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Delete customer's addresses first
            addressDAO.deleteCustomerAddresses(username);
            
            // Delete customer
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_CUSTOMER)) {
                stmt.setString(1, username);
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
    
    public List<Customer> search(String searchTerm) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_CUSTOMERS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = mapRowToCustomer(rs);
                    
                    // Load addresses for this customer
                    List<Address> addresses = addressDAO.findByCustomer(customer.username());
                    
                    // Create a new customer with addresses
                    if (!addresses.isEmpty()) {
                        customer = new Customer(
                            customer.username(),
                            customer.email(),
                            customer.phone(),
                            customer.firstName(),
                            customer.lastName(),
                            customer.password(),
                            customer.status(),
                            addresses,
                            customer.createdAt(),
                            customer.updatedAt()
                        );
                    }
                    
                    customers.add(customer);
                }
            }
        }
        
        return customers;
    }
    
    public List<String> findAllUsernames() throws SQLException {
        List<String> usernames = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM customers")) {
            
            while (rs.next()) {
                usernames.add(rs.getString("username"));
            }
        }
        
        return usernames;
    }
    
    public Address addAddress(Address address) throws SQLException {
        return addressDAO.insert(address);
    }
    
    public void updateAddress(Address address) throws SQLException {
        addressDAO.update(address);
    }
    
    public void deleteAddress(int addressId) throws SQLException {
        addressDAO.delete(addressId);
    }
    
    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                CustomerStatus.fromString(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
} 