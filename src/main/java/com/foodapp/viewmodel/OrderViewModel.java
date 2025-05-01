package com.foodapp.viewmodel;

import com.foodapp.dao.CustomerDAO;
import com.foodapp.dao.OrderDAO;
import com.foodapp.dao.RestaurantDAO;
import com.foodapp.dao.RiderDAO;
import com.foodapp.model.Order;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Rider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderViewModel {
    private final OrderDAO orderDAO;
    private final RiderDAO riderDAO;
    private final CustomerDAO customerDAO;
    private final RestaurantDAO restaurantDAO;
    private final ObservableList<Order> orders;
    private final ObservableList<OrderItem> orderItems;

    public OrderViewModel() {
        try {
            this.orderDAO = new OrderDAO();
            this.riderDAO = new RiderDAO();
            this.customerDAO = new CustomerDAO();
            this.restaurantDAO = new RestaurantDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }
        this.orders = FXCollections.observableArrayList();
        this.orderItems = FXCollections.observableArrayList();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ObservableList<OrderItem> getOrderItems() {
        return orderItems;
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
} 