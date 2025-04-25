package com.foodapp.service;

import com.foodapp.dao.DatabaseManager;
import com.foodapp.model.Order;
import com.foodapp.model.Order.OrderStatus;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Payment;
import com.foodapp.model.Payment.PaymentStatus;
import com.foodapp.model.Promotion;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {
    
    private final PromotionService promotionService;
    
    public OrderService() {
        this.promotionService = new PromotionService();
    }
    
    /**
     * Creates a new order with the given details
     * 
     * @param username The customer's username
     * @param restaurantSlug The restaurant's slug
     * @param items The list of order items
     * @return The created order
     * @throws SQLException If a database error occurs
     */
    public Order createOrder(String username, String restaurantSlug, List<OrderItemInput> items) 
            throws SQLException {
        // Generate a unique order code
        String orderCode = generateOrderCode();
        LocalDateTime now = LocalDateTime.now();
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Insert order
            String insertOrderSql = 
                    "INSERT INTO orders (order_code, customer_username, restaurant_slug, status, " +
                    "total_amount, discount_amount, placed_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, 0, 0, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql)) {
                stmt.setString(1, orderCode);
                stmt.setString(2, username);
                stmt.setString(3, restaurantSlug);
                stmt.setString(4, OrderStatus.PENDING.name());
                stmt.setTimestamp(5, Timestamp.valueOf(now));
                stmt.setTimestamp(6, Timestamp.valueOf(now));
                
                stmt.executeUpdate();
            }
            
            // Insert order items
            for (OrderItemInput item : items) {
                insertOrderItem(conn, orderCode, item);
            }
            
            conn.commit();
            
            // Calculate initial subtotal (without applying promotions yet)
            BigDecimal subtotal = calculateSubtotal(orderCode);
            
            // Return the newly created order with its items
            return new Order(
                    orderCode,
                    username,
                    restaurantSlug,
                    OrderStatus.PENDING,
                    subtotal,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    getOrderItems(orderCode),
                    now,
                    now
            );
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Calculates the subtotal for an order
     * 
     * @param orderCode The order code
     * @return The subtotal amount
     * @throws SQLException If a database error occurs
     */
    public BigDecimal calculateSubtotal(String orderCode) throws SQLException {
        String sql = "SELECT SUM(quantity * price_at_order_time) AS subtotal FROM order_items WHERE order_code = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("subtotal");
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Finalizes an order by setting the total, discount, and status
     * 
     * @param orderCode The order code
     * @param discountAmount The discount amount to apply
     * @return The updated order
     * @throws SQLException If a database error occurs
     */
    public Order finalizeOrder(String orderCode, BigDecimal discountAmount) throws SQLException {
        BigDecimal subtotal = calculateSubtotal(orderCode);
        BigDecimal total = subtotal.subtract(discountAmount);
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Update order
            String updateOrderSql = 
                    "UPDATE orders SET status = ?, total_amount = ?, discount_amount = ?, updated_at = ? " +
                    "WHERE order_code = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                stmt.setString(1, OrderStatus.PREPARING.name());
                stmt.setBigDecimal(2, total);
                stmt.setBigDecimal(3, discountAmount);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(5, orderCode);
                
                stmt.executeUpdate();
            }
            
            // Create payment record
            String insertPaymentSql = 
                    "INSERT INTO payments (order_code, amount, payment_status, payment_method, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertPaymentSql)) {
                LocalDateTime now = LocalDateTime.now();
                
                stmt.setString(1, orderCode);
                stmt.setBigDecimal(2, total);
                stmt.setString(3, PaymentStatus.INITIATED.name());
                stmt.setString(4, Payment.PaymentMethod.CASH.name());  // Default to cash, can be updated later
                stmt.setTimestamp(5, Timestamp.valueOf(now));
                stmt.setTimestamp(6, Timestamp.valueOf(now));
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            
            return getOrder(orderCode);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Applies a promotion code to an order
     * 
     * @param orderCode The order code
     * @param promoCode The promotion code to apply
     * @return The updated order
     * @throws SQLException If a database error occurs
     * @throws IllegalStateException If the promotion is invalid or cannot be applied
     */
    public Order applyPromotion(String orderCode, String promoCode) 
            throws SQLException, IllegalStateException {
        // Validate promotion
        Promotion promotion = promotionService.getValidPromotion(promoCode);
        if (promotion == null) {
            throw new IllegalStateException("Invalid promotion code or the promotion is not active");
        }
        
        // Calculate discount
        BigDecimal subtotal = calculateSubtotal(orderCode);
        BigDecimal discountAmount = promotion.calculateDiscount(subtotal);
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Insert into order_promotions
            String insertOrderPromotionSql = 
                    "INSERT INTO order_promotions (order_code, promotion_code, created_at) " +
                    "VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderPromotionSql)) {
                stmt.setString(1, orderCode);
                stmt.setString(2, promoCode);
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                
                stmt.executeUpdate();
            }
            
            // Update promotion usage count
            String updatePromotionSql = 
                    "UPDATE promotions SET usage_count = usage_count + 1, updated_at = ? " +
                    "WHERE code = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updatePromotionSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(2, promoCode);
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            
            // Finalize order with calculated discount
            return finalizeOrder(orderCode, discountAmount);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Marks an order as paid
     * 
     * @param orderCode The order code
     * @param transactionId The payment transaction ID
     * @return The updated order
     * @throws SQLException If a database error occurs
     */
    public Order markPaid(String orderCode, String transactionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Update payment
            String updatePaymentSql = 
                    "UPDATE payments SET payment_status = ?, transaction_id = ?, updated_at = ? " +
                    "WHERE order_code = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updatePaymentSql)) {
                stmt.setString(1, PaymentStatus.PAID.name());
                stmt.setString(2, transactionId);
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(4, orderCode);
                
                stmt.executeUpdate();
            }
            
            // Update order status
            String updateOrderSql = 
                    "UPDATE orders SET status = ?, updated_at = ? WHERE order_code = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                stmt.setString(1, OrderStatus.DELIVERED.name());
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(3, orderCode);
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            
            return getOrder(orderCode);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Assigns a rider to an order
     * 
     * @param orderCode The order code
     * @param riderId The rider's ID
     * @return The updated order
     * @throws SQLException If a database error occurs
     */
    public Order assignRider(String orderCode, String riderId) throws SQLException {
        String sql = "UPDATE orders SET rider_id = ?, status = ?, updated_at = ? WHERE order_code = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, riderId);
            stmt.setString(2, OrderStatus.IN_TRANSIT.name());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, orderCode);
            
            stmt.executeUpdate();
            
            return getOrder(orderCode);
        }
    }
    
    /**
     * Updates the status of an order
     * 
     * @param orderCode The order code
     * @param status The new status
     * @return The updated order
     * @throws SQLException If a database error occurs
     */
    public Order updateOrderStatus(String orderCode, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE order_code = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, orderCode);
            
            stmt.executeUpdate();
            
            return getOrder(orderCode);
        }
    }
    
    /**
     * Gets an order by its code
     * 
     * @param orderCode The order code
     * @return The order
     * @throws SQLException If a database error occurs
     */
    public Order getOrder(String orderCode) throws SQLException {
        String sql = 
                "SELECT o.order_code, o.customer_username, o.restaurant_slug, o.status, " +
                "o.total_amount, o.discount_amount, o.delivery_address, o.special_instructions, " +
                "o.rider_id, o.placed_at, o.updated_at " +
                "FROM orders o WHERE o.order_code = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                            rs.getString("order_code"),
                            rs.getString("customer_username"),
                            rs.getString("restaurant_slug"),
                            OrderStatus.fromString(rs.getString("status")),
                            rs.getBigDecimal("total_amount"),
                            rs.getBigDecimal("discount_amount"),
                            rs.getString("delivery_address"),
                            rs.getString("special_instructions"),
                            rs.getString("rider_id"),
                            getOrderItems(orderCode),
                            rs.getTimestamp("placed_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets all orders with items
     * 
     * @return List of orders
     * @throws SQLException If a database error occurs
     */
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        String sql = 
                "SELECT o.order_code, o.customer_username, o.restaurant_slug, o.status, " +
                "o.total_amount, o.discount_amount, o.delivery_address, o.special_instructions, " +
                "o.rider_id, o.placed_at, o.updated_at " +
                "FROM orders o ORDER BY o.placed_at DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String orderCode = rs.getString("order_code");
                
                orders.add(new Order(
                        orderCode,
                        rs.getString("customer_username"),
                        rs.getString("restaurant_slug"),
                        OrderStatus.fromString(rs.getString("status")),
                        rs.getBigDecimal("total_amount"),
                        rs.getBigDecimal("discount_amount"),
                        rs.getString("delivery_address"),
                        rs.getString("special_instructions"),
                        rs.getString("rider_id"),
                        getOrderItems(orderCode),
                        rs.getTimestamp("placed_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        }
        
        return orders;
    }
    
    /**
     * Gets order items for a specific order
     * 
     * @param orderCode The order code
     * @return List of order items
     * @throws SQLException If a database error occurs
     */
    private List<OrderItem> getOrderItems(String orderCode) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        
        String sql = 
                "SELECT id, order_code, menu_item_id, menu_item_name, quantity, price_at_order_time, special_instructions " +
                "FROM order_items WHERE order_code = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getInt("id"),
                            rs.getString("order_code"),
                            rs.getInt("menu_item_id"),
                            rs.getString("menu_item_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price_at_order_time"),
                            rs.getString("special_instructions")
                    ));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Inserts an order item
     * 
     * @param conn The database connection
     * @param orderCode The order code
     * @param item The order item input
     * @throws SQLException If a database error occurs
     */
    private void insertOrderItem(Connection conn, String orderCode, OrderItemInput item) throws SQLException {
        String sql = 
                "INSERT INTO order_items (order_code, menu_item_id, menu_item_name, quantity, price_at_order_time, special_instructions) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderCode);
            stmt.setInt(2, item.menuItemId());
            stmt.setString(3, item.menuItemName());
            stmt.setInt(4, item.quantity());
            stmt.setBigDecimal(5, item.price());
            stmt.setString(6, item.specialInstructions());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Generates a unique order code
     * 
     * @return A unique order code
     */
    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Input class for creating order items
     */
    public record OrderItemInput(
            int menuItemId,
            String menuItemName,
            int quantity,
            BigDecimal price,
            String specialInstructions
    ) {}
    
    /**
     * Inner class for promotion service
     */
    private static class PromotionService {
        
        /**
         * Gets a valid promotion by code
         * 
         * @param promoCode The promotion code
         * @return The promotion if valid, null otherwise
         * @throws SQLException If a database error occurs
         */
        public Promotion getValidPromotion(String promoCode) throws SQLException {
            String sql = 
                    "SELECT code, description, type, value, start_date, end_date, status, " +
                    "restaurant_slug, usage_limit, usage_count, created_at, updated_at " +
                    "FROM promotions WHERE code = ? AND status = 'ACTIVE' " +
                    "AND CURRENT_TIMESTAMP BETWEEN start_date AND end_date " +
                    "AND (usage_limit = 0 OR usage_count < usage_limit)";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, promoCode);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Promotion(
                                rs.getString("code"),
                                rs.getString("description"),
                                Promotion.PromotionType.fromString(rs.getString("type")),
                                rs.getBigDecimal("value"),
                                rs.getTimestamp("start_date").toLocalDateTime(),
                                rs.getTimestamp("end_date").toLocalDateTime(),
                                Promotion.PromotionStatus.fromString(rs.getString("status")),
                                rs.getString("restaurant_slug"),
                                rs.getInt("usage_limit"),
                                rs.getInt("usage_count"),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getTimestamp("updated_at").toLocalDateTime()
                        );
                    }
                }
            }
            
            return null;
        }
    }
} 