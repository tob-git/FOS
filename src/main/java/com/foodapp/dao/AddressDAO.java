package com.foodapp.dao;

import com.foodapp.model.Address;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    /*  ─── Stored‑procedure call strings ─────────────────────── */
    private static final String SP_SELECT_BY_CUSTOMER =
            "{ call sp_GetAddressesByCustomer(?) }";

    private static final String SP_INSERT_ADDRESS =
            "{ call sp_InsertAddress(?,?,?,?,?,?,?, ?, ?) }";   // last ? is OUT id

    private static final String SP_UPDATE_ADDRESS =
            "{ call sp_UpdateAddress(?,?,?,?,?,?,?,?) }";

    private static final String SP_DELETE_ADDRESS =
            "{ call sp_DeleteAddress(?) }";

    private static final String SP_DELETE_CUSTOMER_ADDRESSES =
            "{ call sp_DeleteCustomerAddresses(?) }";

    /*  ─── Read  ─────────────────────────────────────────────── */
    public List<Address> findByCustomer(String customerUsername) throws SQLException {
        List<Address> addresses = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_SELECT_BY_CUSTOMER)) {

            stmt.setString(1, customerUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRowToAddress(rs));
                }
            }
        }
        return addresses;
    }

    /*  ─── Create  ───────────────────────────────────────────── */
    public Address insert(Address address) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_INSERT_ADDRESS)) {

            stmt.setString(1, address.street());
            stmt.setString(2, address.city());
            stmt.setString(3, address.state());
            stmt.setString(4, address.postalCode());
            stmt.setString(5, address.country());
            if (address.latitude() != null)
                stmt.setBigDecimal(6, address.latitude());
            else
                stmt.setNull(6, Types.DECIMAL);
            if (address.longitude() != null)
                stmt.setBigDecimal(7, address.longitude());
            else
                stmt.setNull(7, Types.DECIMAL);
            stmt.setString(8, address.customerUsername());

            // 9th parameter is the OUT param
            stmt.registerOutParameter(9, Types.INTEGER);

            stmt.execute();         // executes the procedure

            int newId = stmt.getInt(9);
            return new Address(newId,
                               address.street(), address.city(), address.state(),
                               address.postalCode(), address.country(),
                               address.latitude(), address.longitude(),
                               address.customerUsername());
        }
    }

    /*  ─── Update  ───────────────────────────────────────────── */
    public void update(Address address) throws SQLException {
        if (address.isNew())
            throw new IllegalArgumentException("Cannot update an address without an ID");

        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_UPDATE_ADDRESS)) {

            stmt.setInt   (1, address.id());
            stmt.setString(2, address.street());
            stmt.setString(3, address.city());
            stmt.setString(4, address.state());
            stmt.setString(5, address.postalCode());
            stmt.setString(6, address.country());
            if (address.latitude() != null)
                stmt.setBigDecimal(7, address.latitude());
            else
                stmt.setNull(7, Types.DECIMAL);
            if (address.longitude() != null)
                stmt.setBigDecimal(8, address.longitude());
            else
                stmt.setNull(8, Types.DECIMAL);

            stmt.execute();
        }
    }

    /*  ─── Delete one  ───────────────────────────────────────── */
    public void delete(int addressId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_DELETE_ADDRESS)) {

            stmt.setInt(1, addressId);
            stmt.execute();
        }
    }

    /*  ─── Delete all for a customer  ────────────────────────── */
    public void deleteCustomerAddresses(String customerUsername) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement stmt = conn.prepareCall(SP_DELETE_CUSTOMER_ADDRESSES)) {

            stmt.setString(1, customerUsername);
            stmt.execute();
        }
    }

    /*  ─── Row‑mapper (unchanged)  ───────────────────────────── */
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
