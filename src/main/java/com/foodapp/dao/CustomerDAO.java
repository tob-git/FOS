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

    /* ─────────── Stored‑procedure call texts ─────────── */
    private static final String SP_GET_ALL          = "{ call sp_GetAllCustomers() }";
    private static final String SP_GET_BY_USERNAME  = "{ call sp_GetCustomerByUsername(?) }";
    private static final String SP_INSERT           = "{ call sp_InsertCustomer(?,?,?,?,?,?,?, ?, ?) }";
    private static final String SP_UPDATE           = "{ call sp_UpdateCustomer(?,?,?,?,?,?,?, ?) }";
    private static final String SP_DELETE           = "{ call sp_DeleteCustomer(?) }";
    private static final String SP_SEARCH           = "{ call sp_SearchCustomers(?) }";
    private static final String SP_GET_USERNAMES    = "{ call sp_GetAllCustomerUsernames() }";

    private final AddressDAO addressDAO;

    public CustomerDAO() throws SQLException {
        this.addressDAO = new AddressDAO();
    }

    /* ─────────────────────────── read all ─────────────────────────── */
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_GET_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(toCustomerWithAddresses(rs));
            }
        }
        return customers;
    }

    /* ─────────────────────── read one ────────────────────────────── */
    public Optional<Customer> findByUsername(String username) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_GET_BY_USERNAME)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(toCustomerWithAddresses(rs));
                }
            }
        }
        return Optional.empty();
    }

    /* ─────────────────────────── create ──────────────────────────── */
    public void insert(Customer c) throws SQLException {
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_INSERT)) {

            stmt.setString(1, c.username());
            stmt.setString(2, c.email());
            stmt.setString(3, c.phone());
            stmt.setString(4, c.firstName());
            stmt.setString(5, c.lastName());
            stmt.setString(6, c.password());
            stmt.setString(7, c.status().name());
            stmt.setTimestamp(8, Timestamp.valueOf(c.createdAt() != null ? c.createdAt() : now));
            stmt.setTimestamp(9, Timestamp.valueOf(c.updatedAt() != null ? c.updatedAt() : now));

            conn.setAutoCommit(false);
            stmt.execute();

            if (c.addresses() != null)
                for (Address a : c.addresses())
                    addressDAO.insert(a);

            conn.commit();
        }
    }

    /* ─────────────────────────── update ──────────────────────────── */
    public void update(Customer c) throws SQLException {
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_UPDATE)) {

            stmt.setString(1, c.username());
            stmt.setString(2, c.email());
            stmt.setString(3, c.phone());
            stmt.setString(4, c.firstName());
            stmt.setString(5, c.lastName());
            stmt.setString(6, c.password());
            stmt.setString(7, c.status().name());
            stmt.setTimestamp(8, Timestamp.valueOf(now));

            stmt.execute();
        }
    }

    /* ─────────────────────────── delete ──────────────────────────── */
    public void delete(String username) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_DELETE)) {

            conn.setAutoCommit(false);

            addressDAO.deleteCustomerAddresses(username); // FK‑safe
            stmt.setString(1, username);
            stmt.execute();

            conn.commit();
        }
    }

    /* ─────────────────────────── search ──────────────────────────── */
    public List<Customer> search(String term) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String pattern = "%" + term + "%";

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_SEARCH)) {

            stmt.setString(1, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(toCustomerWithAddresses(rs));
            }
        }
        return list;
    }

    /* ─────────────────── helper : usernames only ─────────────────── */
    public List<String> findAllUsernames() throws SQLException {
        List<String> names = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_GET_USERNAMES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) names.add(rs.getString(1));
        }
        return names;
    }

    /* ───────────────────────── address passthrough ───────────────── */
    public Address addAddress(Address a)           throws SQLException { return addressDAO.insert(a); }
    public void    updateAddress(Address a)        throws SQLException { addressDAO.update(a);       }
    public void    deleteAddress(int id)           throws SQLException { addressDAO.delete(id);       }

    /* ──────────────────── private mapper helpers ─────────────────── */
    private Customer toCustomerWithAddresses(ResultSet rs) throws SQLException {
        Customer base = mapRowToCustomer(rs);
        List<Address> addrs = addressDAO.findByCustomer(base.username());
        return addrs.isEmpty()
                ? base
                : new Customer(base.username(), base.email(), base.phone(),
                               base.firstName(), base.lastName(), base.password(),
                               base.status(), addrs, base.createdAt(), base.updatedAt());
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
