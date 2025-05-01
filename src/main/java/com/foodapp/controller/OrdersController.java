package com.foodapp.controller;

import com.foodapp.model.Order;
import com.foodapp.model.Order.OrderStatus;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Rider;
import com.foodapp.viewmodel.OrderViewModel;
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
    
    @FXML
    private TextField deliveryAddressField;
    
    @FXML
    private TextField specialInstructionsField;
    
    @FXML
    private ComboBox<Rider> riderComboBox;
    
    @FXML
    private Button editOrderButton;
    
    @FXML
    private TextField orderCodeField;
    
    @FXML
    private ComboBox<String> customerComboBox;
    
    @FXML
    private ComboBox<String> restaurantComboBox;
    
    @FXML
    private Button createOrderButton;
    
    @FXML
    private TextField newDeliveryAddressField;
    
    @FXML
    private TextField newSpecialInstructionsField;
    
    private final OrderViewModel orderViewModel;
    
    public OrdersController() {
        this.orderViewModel = new OrderViewModel();
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
                editOrderButton.setDisable(false);
                statusComboBox.setValue(newSelection.status());
                
                // Fill in the edit fields
                deliveryAddressField.setText(newSelection.deliveryAddress());
                specialInstructionsField.setText(newSelection.specialInstructions());
                
                // Set the rider if assigned
                if (newSelection.riderId() != null && !newSelection.riderId().isEmpty()) {
                    for (Rider rider : riderComboBox.getItems()) {
                        if (rider.id().equals(newSelection.riderId())) {
                            riderComboBox.setValue(rider);
                            break;
                        }
                    }
                } else {
                    riderComboBox.setValue(null);
                }
                
                try {
                    displayOrderDetails(newSelection.orderCode());
                } catch (SQLException e) {
                    showErrorAlert("Error", "Failed to load order details: " + e.getMessage());
                }
            } else {
                updateStatusBtn.setDisable(true);
                editOrderButton.setDisable(true);
                statusComboBox.setValue(null);
                clearOrderDetails();
            }
        });
        
        // Load data from database
        loadOrders();
        
        // Load riders for assignment
        loadRiders();
        
        // Initialize create order button
        createOrderButton.setDisable(false);
        
        // Load customers and restaurants for new order creation
        loadCustomersAndRestaurants();
    }
    
    private void loadOrders() {
        try {
            orderViewModel.loadOrders();
            ordersTableView.setItems(orderViewModel.getOrders());
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }
    
    private void displayOrderDetails(String orderCode) throws SQLException {
        orderViewModel.loadOrderItems(orderCode);
        orderItemsTableView.setItems(orderViewModel.getOrderItems());
        
        Order order = ordersTableView.getSelectionModel().getSelectedItem();
        if (order != null) {
            customerInfoLabel.setText("Customer: " + order.customerUsername());
            restaurantInfoLabel.setText("Restaurant: " + order.restaurantSlug());
            orderDetailsLabel.setText("Order #" + order.orderCode() + " - " + order.status());
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
                orderViewModel.searchOrders(searchText);
                ordersTableView.setItems(orderViewModel.getOrders());
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
                orderViewModel.updateOrderStatus(updatedOrder);
                ordersTableView.setItems(orderViewModel.getOrders());
                
                // Select the updated order again
                for (Order order : orderViewModel.getOrders()) {
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
    
    @FXML
    private void handleEditOrder() {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                // Get the new delivery address and special instructions
                String newDeliveryAddress = deliveryAddressField.getText();
                String newSpecialInstructions = specialInstructionsField.getText();
                
                // Get the new rider
                Rider selectedRider = riderComboBox.getValue();
                String newRiderId = selectedRider != null ? selectedRider.id() : null;
                
                // Create updated order with new values
                Order updatedOrder = new Order(
                    selectedOrder.orderCode(),
                    selectedOrder.customerUsername(),
                    selectedOrder.restaurantSlug(),
                    selectedOrder.status(),
                    selectedOrder.totalAmount(),
                    selectedOrder.discountAmount(),
                    newDeliveryAddress,
                    newSpecialInstructions,
                    newRiderId,
                    selectedOrder.orderItems(),
                    selectedOrder.placedAt(),
                    LocalDateTime.now()
                );
                
                // Update in database
                orderViewModel.updateOrder(updatedOrder);
                
                // Refresh the orders list
                loadOrders();
                
                // Select the updated order again
                for (Order order : orderViewModel.getOrders()) {
                    if (order.orderCode().equals(updatedOrder.orderCode())) {
                        ordersTableView.getSelectionModel().select(order);
                        break;
                    }
                }
                
                showInfoAlert("Success", "Order details updated successfully");
            } catch (SQLException e) {
                showErrorAlert("Update Error", "Failed to update order: " + e.getMessage());
            }
        }
    }
    
    private void loadRiders() {
        try {
            List<Rider> riders = orderViewModel.loadAvailableRiders();
            riderComboBox.setItems(FXCollections.observableArrayList(riders));
            
            // Setup cell factory to display rider names
            riderComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Rider>() {
                @Override
                protected void updateItem(Rider rider, boolean empty) {
                    super.updateItem(rider, empty);
                    if (empty || rider == null) {
                        setText(null);
                    } else {
                        setText(rider.getFullName());
                    }
                }
            });
            
            riderComboBox.setButtonCell(new javafx.scene.control.ListCell<Rider>() {
                @Override
                protected void updateItem(Rider rider, boolean empty) {
                    super.updateItem(rider, empty);
                    if (empty || rider == null) {
                        setText(null);
                    } else {
                        setText(rider.getFullName());
                    }
                }
            });
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load riders: " + e.getMessage());
        }
    }
    
    private void loadCustomersAndRestaurants() {
        try {
            // Load customers
            List<String> customerUsernames = orderViewModel.loadCustomerUsernames();
            customerComboBox.setItems(FXCollections.observableArrayList(customerUsernames));
            
            // Load restaurants
            List<String> restaurantSlugs = orderViewModel.loadRestaurantSlugs();
            restaurantComboBox.setItems(FXCollections.observableArrayList(restaurantSlugs));
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load customers and restaurants: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateOrder() {
        // Validate form fields
        if (orderCodeField.getText().trim().isEmpty() ||
            customerComboBox.getValue() == null ||
            restaurantComboBox.getValue() == null ||
            newDeliveryAddressField.getText().trim().isEmpty()) {
            
            showErrorAlert("Validation Error", "Please fill in all required fields");
            return;
        }
        
        try {
            // Create a new order
            String orderCode = orderCodeField.getText().trim();
            String customerUsername = customerComboBox.getValue();
            String restaurantSlug = restaurantComboBox.getValue();
            String deliveryAddress = newDeliveryAddressField.getText().trim();
            String specialInstructions = newSpecialInstructionsField.getText().trim();
            OrderStatus status = OrderStatus.PENDING;
            LocalDateTime now = LocalDateTime.now();
            
            // Create initial order with no items, 0 total amount
            Order newOrder = new Order(
                orderCode,
                customerUsername,
                restaurantSlug,
                status,
                new java.math.BigDecimal("0.00"),  // Total amount (will be updated when items are added)
                new java.math.BigDecimal("0.00"),  // Discount amount
                deliveryAddress,
                specialInstructions,
                null,                              // No rider assigned yet
                List.of(),                         // Empty items list
                now,                               // placed at
                now                                // updated at
            );
            
            // Save the order
            orderViewModel.createOrder(newOrder);
            
            // Refresh the orders list
            loadOrders();
            
            // Clear the form
            orderCodeField.clear();
            customerComboBox.setValue(null);
            restaurantComboBox.setValue(null);
            newDeliveryAddressField.clear();
            newSpecialInstructionsField.clear();
            
            showInfoAlert("Success", "Order created successfully. Add items to the order.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to create order: " + e.getMessage());
        }
    }
    
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 