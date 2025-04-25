package com.foodapp.controller;

import com.foodapp.dao.OrderDAO;
import com.foodapp.model.Order;
import com.foodapp.model.Order.OrderStatus;
import com.foodapp.model.OrderItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Orders view
 */
public class OrdersController {

    @FXML
    private TableView<Order> ordersTableView;
    
    @FXML
    private TableColumn<Order, String> orderCodeColumn;
    
    @FXML
    private TableColumn<Order, String> customerColumn;
    
    @FXML
    private TableColumn<Order, String> restaurantColumn;
    
    @FXML
    private TableColumn<Order, OrderStatus> statusColumn;
    
    @FXML
    private TableColumn<Order, String> totalColumn;
    
    @FXML
    private TableColumn<Order, String> dateColumn;
    
    @FXML
    private TableView<OrderItem> orderItemsTableView;
    
    @FXML
    private TableColumn<OrderItem, String> itemNameColumn;
    
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    
    @FXML
    private TableColumn<OrderItem, String> priceColumn;
    
    @FXML
    private TableColumn<OrderItem, String> subtotalColumn;
    
    @FXML
    private ComboBox<OrderStatus> statusComboBox;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button updateStatusBtn;
    
    @FXML
    private Label customerInfoLabel;
    
    @FXML
    private Label restaurantInfoLabel;
    
    @FXML
    private Label orderDetailsLabel;
    
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private final OrderDAO orderDAO;
    
    public OrdersController() {
        this.orderDAO = new OrderDAO();
    }
    
    @FXML
    private void initialize() {
        // Initialize table columns
        orderCodeColumn.setCellValueFactory(new PropertyValueFactory<>("orderCode"));
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerUsername()));
        restaurantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().restaurantSlug()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTotal()));
        
        // Format date column
        dateColumn.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(data.getValue().placedAt().format(formatter));
        });
        
        // Initialize order items table
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedPrice()));
        subtotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedSubtotal()));
        
        // Set up status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        
        // Setup selection listener for orders table
        ordersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateStatusBtn.setDisable(false);
                statusComboBox.setValue(newSelection.status());
                try {
                    displayOrderDetails(newSelection.orderCode());
                } catch (SQLException e) {
                    showErrorAlert("Error", "Failed to load order details: " + e.getMessage());
                }
            } else {
                updateStatusBtn.setDisable(true);
                statusComboBox.setValue(null);
                clearOrderDetails();
            }
        });
        
        // Load data from database
        loadOrders();
    }
    
    private void loadOrders() {
        try {
            List<Order> ordersList = orderDAO.findAll();
            orders.clear();
            orders.addAll(ordersList);
            ordersTableView.setItems(orders);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }
    
    private void displayOrderDetails(String orderCode) throws SQLException {
        Order order = orderDAO.findByCode(orderCode);
        if (order != null) {
            customerInfoLabel.setText("Customer: " + order.customerUsername());
            restaurantInfoLabel.setText("Restaurant: " + order.restaurantSlug());
            orderDetailsLabel.setText("Order #" + order.orderCode() + " - " + order.status());
            
            // Display order items
            List<OrderItem> items = orderDAO.findOrderItems(orderCode);
            orderItemsTableView.setItems(FXCollections.observableArrayList(items));
        }
    }
    
    private void clearOrderDetails() {
        customerInfoLabel.setText("Customer: ");
        restaurantInfoLabel.setText("Restaurant: ");
        orderDetailsLabel.setText("Order Details");
        orderItemsTableView.getItems().clear();
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadOrders();
        } else {
            try {
                List<Order> searchResults = orderDAO.search(searchText);
                orders.clear();
                orders.addAll(searchResults);
                ordersTableView.setItems(orders);
            } catch (SQLException e) {
                showErrorAlert("Search Error", "Failed to search orders: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleUpdateStatus() {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        OrderStatus newStatus = statusComboBox.getValue();
        
        if (selectedOrder != null && newStatus != null) {
            try {
                // Create updated order with new status
                Order updatedOrder = new Order(
                    selectedOrder.orderCode(),
                    selectedOrder.customerUsername(),
                    selectedOrder.restaurantSlug(),
                    newStatus,
                    selectedOrder.totalAmount(),
                    selectedOrder.discountAmount(),
                    selectedOrder.deliveryAddress(),
                    selectedOrder.specialInstructions(),
                    selectedOrder.riderId(),
                    selectedOrder.orderItems(),
                    selectedOrder.placedAt(),
                    LocalDateTime.now()
                );
                
                // Update in database
                orderDAO.update(updatedOrder);
                
                // Refresh data
                loadOrders();
                
                // Select the updated order again
                for (Order order : orders) {
                    if (order.orderCode().equals(updatedOrder.orderCode())) {
                        ordersTableView.getSelectionModel().select(order);
                        break;
                    }
                }
            } catch (SQLException e) {
                showErrorAlert("Update Error", "Failed to update order status: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadOrders();
    }
    
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 