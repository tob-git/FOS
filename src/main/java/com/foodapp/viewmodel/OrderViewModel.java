package com.foodapp.viewmodel;

import com.foodapp.dao.AddressDAO;
import com.foodapp.dao.CustomerDAO;
import com.foodapp.dao.OrderDAO;
import com.foodapp.dao.RestaurantDAO;
import com.foodapp.dao.RiderDAO;
import com.foodapp.dao.PromotionDAO;
import com.foodapp.dao.DatabaseManager;
import com.foodapp.model.Address;
import com.foodapp.model.Order;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Rider;
import com.foodapp.model.MenuItem;
import com.foodapp.model.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OrderViewModel {
    private final OrderDAO orderDAO;
    private final RiderDAO riderDAO;
    private final CustomerDAO customerDAO;
    private final RestaurantDAO restaurantDAO;
    private final AddressDAO addressDAO;
    private final PromotionDAO promotionDAO;
    private final ObservableList<Order> orders;
    private final ObservableList<OrderItem> orderItems;
    private final ObservableList<MenuItem> menuItems;
    private final ObservableList<Address> customerAddresses;

    public OrderViewModel() {
        try {
            this.orderDAO = new OrderDAO();
            this.riderDAO = new RiderDAO();
            this.customerDAO = new CustomerDAO();
            this.restaurantDAO = new RestaurantDAO();
            this.addressDAO = new AddressDAO();
            this.promotionDAO = new PromotionDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }
        this.orders = FXCollections.observableArrayList();
        this.orderItems = FXCollections.observableArrayList();
        this.menuItems = FXCollections.observableArrayList();
        this.customerAddresses = FXCollections.observableArrayList();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ObservableList<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public ObservableList<MenuItem> getMenuItems() {
        return menuItems;
    }
    
    public ObservableList<Address> getCustomerAddresses() {
        return customerAddresses;
    }

    public void loadOrders() throws SQLException {
        List<Order> ordersList = orderDAO.findAll();
        orders.clear();
        orders.addAll(ordersList);
    }

    public void searchOrders(String searchText) throws SQLException {
        List<Order> searchResults = orderDAO.search(searchText);
        orders.clear();
        orders.addAll(searchResults);
    }

    public void loadOrderItems(String orderCode) throws SQLException {
        List<OrderItem> items = orderDAO.findOrderItems(orderCode);
        orderItems.clear();
        orderItems.addAll(items);
    }

    public void updateOrderStatus(Order order) throws SQLException {
        orderDAO.update(order);
        loadOrders();
    }
    
    public void updateOrder(Order order) throws SQLException {
        orderDAO.update(order);
        loadOrders();
    }
    
    public List<Rider> loadAvailableRiders() throws SQLException {
        // Load riders that are active and not on delivery
        return riderDAO.findAvailableRiders();
    }
    
    public List<String> loadCustomerUsernames() throws SQLException {
        return customerDAO.findAllUsernames();
    }
    
    public List<String> loadRestaurantSlugs() throws SQLException {
        return restaurantDAO.findAllSlugs();
    }
    
    public void createOrder(Order order) throws SQLException {
        orderDAO.insert(order);
        loadOrders();
    }
    
    public String generateOrderCode() {
        return orderDAO.generateOrderCode();
    }
    
    public void addPayment(String orderCode, BigDecimal amount, String paymentMethod, String transactionId) throws SQLException {
        orderDAO.addPayment(orderCode, amount, paymentMethod, transactionId);
    }
    
    public void loadCustomerAddresses(String customerUsername) throws SQLException {
        List<Address> addresses = addressDAO.findByCustomer(customerUsername);
        customerAddresses.clear();
        customerAddresses.addAll(addresses);
    }
    
    public void loadMenuItemsByRestaurant(String restaurantSlug) throws SQLException {
        List<MenuItem> items = orderDAO.findMenuItemsByRestaurant(restaurantSlug);
        menuItems.clear();
        menuItems.addAll(items);
    }
    
    public void addOrderItem(String orderCode, MenuItem menuItem, int quantity, String specialInstructions) throws SQLException {
        OrderItem orderItem = new OrderItem(
            0, // ID will be assigned by the database
            orderCode,
            menuItem.id(),
            menuItem.name(),
            quantity,
            menuItem.price(),
            specialInstructions
        );
        
        // Add the order item to the database
        orderDAO.addOrderItem(orderItem);
        
        // Reload order items
        loadOrderItems(orderCode);
    }
    
    /**
     * Gets the calculated total amount for an order
     * 
     * @param orderCode The order code
     * @return The calculated total amount
     * @throws SQLException If a database error occurs
     */
    public BigDecimal getOrderTotal(String orderCode) throws SQLException {
        return orderDAO.calculateOrderTotal(orderCode);
    }
    
    /**
     * Reloads orders after item changes and recalculates discount if needed
     * 
     * @param orderCode The order code
     * @throws SQLException If a database error occurs
     */
    public void recalculateOrderTotal(String orderCode) throws SQLException {
        // Check if there's a promo code applied to this order
        String promoCode = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT op.promotion_code FROM order_promotions op WHERE op.order_code = ? ORDER BY op.created_at DESC LIMIT 1")) {
             
            stmt.setString(1, orderCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    promoCode = rs.getString("promotion_code");
                }
            }
        } catch (SQLException e) {
            // Table might not exist, skip this step
        }
        
        // If there's a promo code, recalculate the discount
        if (promoCode != null && !promoCode.isEmpty()) {
            try {
                // Calculate new discount based on updated order items
                BigDecimal newDiscount = calculateDiscountFromPromoCode(promoCode, orderCode);
                
                // Update the discount amount in the order
                try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE orders SET discount_amount = ?, updated_at = ? WHERE order_code = ?")) {
                    
                    stmt.setBigDecimal(1, newDiscount);
                    stmt.setObject(2, java.time.LocalDateTime.now());
                    stmt.setString(3, orderCode);
                    
                    stmt.executeUpdate();
                }
            } catch (IllegalStateException e) {
                // Promotion might not be valid anymore, just continue
            }
        }
        
        // Reload orders to reflect the updated totals
        loadOrders();
    }

    /**
     * Calculates discount amount from a promotion code
     * 
     * @param promoCode The promotion code to use
     * @param orderCode The order code (optional, can be null for new orders)
     * @return The calculated discount amount
     * @throws SQLException If a database error occurs
     * @throws IllegalStateException If the promotion is invalid
     */
    public BigDecimal calculateDiscountFromPromoCode(String promoCode, String orderCode) throws SQLException, IllegalStateException {
        // Get promotion from database
        Promotion promotion = promotionDAO.findByCode(promoCode);
        
        // Validate the promotion
        if (promotion == null) {
            throw new IllegalStateException("Invalid promotion code");
        }
        
        if (!promotion.isActive()) {
            throw new IllegalStateException("This promotion is not active or has expired");
        }
        
        // Calculate subtotal based on order items if order exists
        BigDecimal subtotal;
        if (orderCode != null && !orderCode.isEmpty()) {
            // For existing orders, calculate from items
            subtotal = orderDAO.calculateOrderTotal(orderCode);
            // Add back any existing discount so we have the true subtotal
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT discount_amount FROM orders WHERE order_code = ?")) {
                
                stmt.setString(1, orderCode);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal existingDiscount = rs.getBigDecimal("discount_amount");
                        if (existingDiscount != null && existingDiscount.compareTo(BigDecimal.ZERO) > 0) {
                            subtotal = subtotal.add(existingDiscount);
                        }
                    }
                }
            }
        } else {
            // For new orders, get the subtotal from the current items in the list
            subtotal = calculateCurrentSubtotal();
        }
        
        // Calculate the discount amount based on promotion type
        return promotion.calculateDiscount(subtotal);
    }

    /**
     * Calculate the subtotal of currently loaded items
     * 
     * @return The subtotal
     */
    private BigDecimal calculateCurrentSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.priceAtOrderTime().multiply(new BigDecimal(item.quantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }

    /**
     * Records which promotion has been applied to an order
     * 
     * @param orderCode The order code
     * @param promoCode The promotion code
     * @throws SQLException If a database error occurs
     */
    public void recordPromotionUsage(String orderCode, String promoCode) throws SQLException {
        if (orderCode == null || promoCode == null || orderCode.isEmpty() || promoCode.isEmpty()) {
            return; // Nothing to record
        }
        
        // First check if the order_promotions table exists
        boolean tableExists = false;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'order_promotions'")) {
             
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
        } catch (SQLException e) {
            // Table does not exist or cannot access information_schema
        }
        
        // If the table doesn't exist, there's nothing to record
        if (!tableExists) {
            return;
        }
        
        // Delete any existing promotion records for this order
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM order_promotions WHERE order_code = ?")) {
             
            stmt.setString(1, orderCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Ignore if table doesn't exist or deletion fails
        }
        
        // Insert new promotion record
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO order_promotions (order_code, promotion_code, created_at) VALUES (?, ?, ?)")) {
             
            stmt.setString(1, orderCode);
            stmt.setString(2, promoCode);
            stmt.setObject(3, java.time.LocalDateTime.now());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Ignore if insertion fails, discount will still work based on discount_amount field
        }
        
        // Increment the usage count of the promotion
        try {
            promotionDAO.incrementUsageCount(promoCode);
        } catch (SQLException e) {
            // Ignore if increment fails
        }
    }
} 