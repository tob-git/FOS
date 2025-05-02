package com.foodapp.dao;

import com.foodapp.model.Order;
import com.foodapp.model.Order.OrderStatus;
import com.foodapp.model.OrderItem;
import com.foodapp.model.MenuItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderDAO {

    private static final String SELECT_ALL_ORDERS = "SELECT * FROM orders ORDER BY placed_at DESC";
    private static final String SELECT_ORDER_BY_CODE = "SELECT * FROM orders WHERE order_code = ?";
    private static final String SELECT_ORDER_ITEMS = "SELECT * FROM order_items WHERE order_code = ?";
    private static final String INSERT_ORDER = "INSERT INTO orders (order_code, customer_username, restaurant_slug, status, discount_amount, address_id, special_instructions, rider_id, placed_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ORDER_ITEM = "INSERT INTO order_items (order_code, menu_item_id, menu_item_name, quantity, price_at_order_time, special_instructions) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ORDER = "UPDATE orders SET customer_username = ?, restaurant_slug = ?, status = ?, discount_amount = ?, address_id = ?, special_instructions = ?, rider_id = ?, updated_at = ? WHERE order_code = ?";
    private static final String DELETE_ORDER = "DELETE FROM orders WHERE order_code = ?";
    private static final String SELECT_ORDERS_BY_CUSTOMER = "SELECT * FROM orders WHERE customer_username = ? ORDER BY placed_at DESC";
    private static final String SELECT_ORDERS_BY_RESTAURANT = "SELECT * FROM orders WHERE restaurant_slug = ? ORDER BY placed_at DESC";
    private static final String SELECT_ORDERS_BY_RIDER = "SELECT * FROM orders WHERE rider_id = ? ORDER BY placed_at DESC";
    private static final String SEARCH_ORDERS = "SELECT * FROM orders WHERE order_code LIKE ? OR customer_username LIKE ? OR restaurant_slug LIKE ? OR status LIKE ? ORDER BY placed_at DESC";
    private static final String INSERT_PAYMENT = "INSERT INTO payments (order_code, amount, payment_status, payment_method, transaction_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_PAYMENTS_BY_ORDER = "SELECT * FROM payments WHERE order_code = ?";
    private static final String UPDATE_PAYMENT = "UPDATE payments SET payment_status = ?, updated_at = ? WHERE id = ?";
    private static final String SELECT_MENU_ITEMS_BY_RESTAURANT = "SELECT mi.* FROM menu_items mi JOIN menus m ON mi.menu_id = m.id WHERE m.restaurant_slug = ? AND mi.is_available = 1 ORDER BY mi.category, mi.name";

    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ORDERS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }
        }
        
        return orders;
    }
    
    public Order findByCode(String orderCode) throws SQLException {
        Order order = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ORDER_BY_CODE)) {
            
            stmt.setString(1, orderCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    order = mapResultSetToOrder(rs);
                    order = new Order(
                        order.orderCode(),
                        order.customerUsername(),
                        order.restaurantSlug(),
                        order.status(),
                        order.discountAmount(),
                        order.addressId(),
                        order.specialInstructions(),
                        order.riderId(),
                        findOrderItems(orderCode),
                        order.placedAt(),
                        order.updatedAt()
                    );
                }
            }
        }
        
        return order;
    }
    
    public List<OrderItem> findOrderItems(String orderCode) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ORDER_ITEMS)) {
            
            stmt.setString(1, orderCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = mapResultSetToOrderItem(rs);
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    public void insert(Order order) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_ORDER)) {
                stmt.setString(1, order.orderCode());
                stmt.setString(2, order.customerUsername());
                stmt.setString(3, order.restaurantSlug());
                stmt.setString(4, order.status().toString());
                stmt.setBigDecimal(5, order.discountAmount());
                stmt.setInt(6, order.addressId());
                stmt.setString(7, order.specialInstructions());
                stmt.setString(8, order.riderId());
                stmt.setObject(9, order.placedAt());
                stmt.setObject(10, order.updatedAt());
                
                stmt.executeUpdate();
            }
            
            if (order.orderItems() != null) {
                try (PreparedStatement stmt = conn.prepareStatement(INSERT_ORDER_ITEM)) {
                    for (OrderItem item : order.orderItems()) {
                        stmt.setString(1, order.orderCode());
                        stmt.setInt(2, item.menuItemId());
                        stmt.setString(3, item.menuItemName());
                        stmt.setInt(4, item.quantity());
                        stmt.setBigDecimal(5, item.priceAtOrderTime());
                        stmt.setString(6, item.specialInstructions());
                        
                        stmt.executeUpdate();
                    }
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
    
    public void update(Order order) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ORDER)) {
            
            stmt.setString(1, order.customerUsername());
            stmt.setString(2, order.restaurantSlug());
            stmt.setString(3, order.status().toString());
            stmt.setBigDecimal(4, order.discountAmount());
            stmt.setInt(5, order.addressId());
            stmt.setString(6, order.specialInstructions());
            stmt.setString(7, order.riderId());
            stmt.setObject(8, order.updatedAt());
            stmt.setString(9, order.orderCode());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String orderCode) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_ORDER)) {
            
            stmt.setString(1, orderCode);
            stmt.executeUpdate();
        }
    }
    
    public List<Order> findByCustomer(String customerUsername) throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ORDERS_BY_CUSTOMER)) {
            
            stmt.setString(1, customerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    public List<Order> findByRestaurant(String restaurantSlug) throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ORDERS_BY_RESTAURANT)) {
            
            stmt.setString(1, restaurantSlug);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    public List<Order> findByRider(String riderId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ORDERS_BY_RIDER)) {
            
            stmt.setString(1, riderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    public List<Order> search(String searchText) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_ORDERS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    public String generateOrderCode() {
        // Format: ORD-YYYYMMDD-XXXX where XXXX is a random alphanumeric string
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        return "ORD-" + datePart + "-" + randomPart;
    }
    
    public List<MenuItem> findMenuItemsByRestaurant(String restaurantSlug) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MENU_ITEMS_BY_RESTAURANT)) {
            
            stmt.setString(1, restaurantSlug);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem menuItem = mapResultSetToMenuItem(rs);
                    menuItems.add(menuItem);
                }
            }
        }
        
        return menuItems;
    }
    
    /**
     * Calculates the total amount for an order based on its items
     * 
     * @param orderCode The order code
     * @return The calculated total amount
     * @throws SQLException If a database error occurs
     */
    public BigDecimal calculateOrderTotal(String orderCode) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        
        // Get all order items
        List<OrderItem> items = findOrderItems(orderCode);
        
        // Sum up the item prices * quantities
        for (OrderItem item : items) {
            BigDecimal itemTotal = item.priceAtOrderTime().multiply(new BigDecimal(item.quantity()));
            System.out.println("Item total: " + itemTotal);
            total = total.add(itemTotal);
        }
        
        // Apply discount if exists
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT discount_amount FROM orders WHERE order_code = ?")) {
            
            stmt.setString(1, orderCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal discount = rs.getBigDecimal("discount_amount");
                    if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                        total = total.subtract(discount);
                    }
                }
            }
        }
        
        return total;
    }
    
    public void addOrderItem(OrderItem orderItem) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ORDER_ITEM, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, orderItem.orderCode());
            stmt.setInt(2, orderItem.menuItemId());
            stmt.setString(3, orderItem.menuItemName());
            stmt.setInt(4, orderItem.quantity());
            stmt.setBigDecimal(5, orderItem.priceAtOrderTime());
            stmt.setString(6, orderItem.specialInstructions());
            
            stmt.executeUpdate();
        }
    }
    
    public void addPayment(String orderCode, BigDecimal amount, String paymentMethod, String transactionId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PAYMENT)) {
            
            LocalDateTime now = LocalDateTime.now();
            
            stmt.setString(1, orderCode);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, "PAID");  // Default to PAID status
            stmt.setString(4, paymentMethod);
            stmt.setString(5, transactionId);
            stmt.setObject(6, now);
            stmt.setObject(7, now);
            
            stmt.executeUpdate();
        }
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        String orderCode = rs.getString("order_code");
        String customerUsername = rs.getString("customer_username");
        String restaurantSlug = rs.getString("restaurant_slug");
        OrderStatus status = OrderStatus.fromString(rs.getString("status"));
        BigDecimal discountAmount = rs.getBigDecimal("discount_amount");
        int addressId = rs.getInt("address_id");
        String specialInstructions = rs.getString("special_instructions");
        String riderId = rs.getString("rider_id");
        LocalDateTime placedAt = rs.getObject("placed_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new Order(
            orderCode,
            customerUsername,
            restaurantSlug,
            status,
            discountAmount,
            addressId,
            specialInstructions,
            riderId,
            placedAt,
            updatedAt
        );
    }
    
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String orderCode = rs.getString("order_code");
        int menuItemId = rs.getInt("menu_item_id");
        String menuItemName = rs.getString("menu_item_name");
        int quantity = rs.getInt("quantity");
        BigDecimal priceAtOrderTime = rs.getBigDecimal("price_at_order_time");
        String specialInstructions = rs.getString("special_instructions");
        
        return new OrderItem(
            id,
            orderCode,
            menuItemId,
            menuItemName,
            quantity,
            priceAtOrderTime,
            specialInstructions
        );
    }
    
    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal price = rs.getBigDecimal("price");
        String imageUrl = rs.getString("image_url");
        MenuItem.MenuItemCategory category = MenuItem.MenuItemCategory.fromString(rs.getString("category"));
        boolean isAvailable = rs.getBoolean("is_available");
        int menuId = rs.getInt("menu_id");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new MenuItem(
            id,
            name,
            description,
            price,
            imageUrl,
            category,
            isAvailable,
            menuId,
            createdAt,
            updatedAt
        );
    }
} 