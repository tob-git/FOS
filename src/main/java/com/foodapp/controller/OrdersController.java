package com.foodapp.controller;

import com.foodapp.model.Address;
import com.foodapp.model.Order;
import com.foodapp.model.Order.OrderStatus;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Rider;
import com.foodapp.model.MenuItem;
import com.foodapp.viewmodel.OrderViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Label promoCodeLabel;
    
    @FXML
    private Label addressInfoLabel;
    
    @FXML
    private ComboBox<Address> addressComboBox;
    
    @FXML
    private TextField specialInstructionsField;
    
    @FXML
    private TextField promoCodeField;
    
    @FXML
    private ComboBox<Rider> riderComboBox;
    
    @FXML
    private Button editOrderButton;
    
    @FXML
    private ComboBox<String> customerComboBox;
    
    @FXML
    private ComboBox<String> restaurantComboBox;
    
    @FXML
    private ComboBox<Address> newAddressComboBox;
    
    @FXML
    private TextField newSpecialInstructionsField;
    
    @FXML
    private TextField newPromoCodeField;
    
    @FXML
    private TextField paymentAmountField;
    
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    
    @FXML
    private TextField transactionIdField;
    
    @FXML
    private Button addPaymentButton;
    
    @FXML
    private Button createOrderButton;
    
    @FXML
    private TableView<MenuItem> menuItemsTableView;
    
    @FXML
    private TableColumn<MenuItem, String> menuItemNameColumn;
    
    @FXML
    private TableColumn<MenuItem, String> menuItemDescriptionColumn;
    
    @FXML
    private TableColumn<MenuItem, String> menuItemPriceColumn;
    
    @FXML
    private TableColumn<MenuItem, String> menuItemCategoryColumn;
    
    @FXML
    private Spinner<Integer> itemQuantitySpinner;
    
    @FXML
    private TextField itemSpecialInstructionsField;
    
    @FXML
    private Button addItemToOrderButton;
    
    @FXML
    private ComboBox<String> orderForItemsComboBox;
    
    private final OrderViewModel orderViewModel;
    
    // Maps to cache address data
    private final Map<String, List<Address>> customerAddressesCache = new HashMap<>();
    
    // Currently selected order for adding items
    private String selectedOrderCode;
    
    public OrdersController() {
        this.orderViewModel = new OrderViewModel();
    }
    
    @FXML
    private void initialize() {
        // Initialize table columns - using lambda expressions instead of PropertyValueFactory
        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderCode()));
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerUsername()));
        restaurantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().restaurantSlug()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().status()));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTotal()));
        
        // Format date column
        dateColumn.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(data.getValue().placedAt().format(formatter));
        });
        
        // Initialize order items table
        itemNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().menuItemName()));
        quantityColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().quantity()).asObject());
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedPrice()));
        subtotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedSubtotal()));
        
        // Initialize menu items table
        menuItemNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        menuItemDescriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().description()));
        menuItemPriceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedPrice()));
        menuItemCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category().toString()));
        
        // Initialize spinner for quantity
        itemQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        
        // Set up status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        
        // Setup payment method combo box
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "CREDIT_CARD", "DEBIT_CARD", "CASH", "MOBILE_PAYMENT", "ONLINE_BANKING"
        ));
        
        // Setup selection listener for orders table
        ordersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateStatusBtn.setDisable(false);
                editOrderButton.setDisable(false);
                addPaymentButton.setDisable(false);
                statusComboBox.setValue(newSelection.status());
                
                // Store the selected order code for adding items
                selectedOrderCode = newSelection.orderCode();
                
                // Add the selected order to the orderForItemsComboBox if not already there
                if (!orderForItemsComboBox.getItems().contains(selectedOrderCode)) {
                    orderForItemsComboBox.getItems().add(selectedOrderCode);
                }
                orderForItemsComboBox.setValue(selectedOrderCode);
                
                // Fill in the edit fields
                specialInstructionsField.setText(newSelection.specialInstructions());
                promoCodeField.setText(""); // Clear the promo code field
                
                // Load customer addresses for this order
                try {
                    loadCustomerAddresses(newSelection.customerUsername());
                    
                    // Set the address in the combo box
                    if (addressComboBox.getItems() != null && !addressComboBox.getItems().isEmpty()) {
                        for (Address address : addressComboBox.getItems()) {
                            if (address.id() == newSelection.addressId()) {
                                addressComboBox.setValue(address);
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    showErrorAlert("Database Error", "Failed to load customer addresses: " + e.getMessage());
                }
                
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
                addPaymentButton.setDisable(true);
                statusComboBox.setValue(null);
                clearOrderDetails();
            }
        });
        
        // Setup listener for orderForItemsComboBox
        orderForItemsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedOrderCode = newVal;
                // Find the order from the ordersTableView
                for (Order order : ordersTableView.getItems()) {
                    if (order.orderCode().equals(selectedOrderCode)) {
                        try {
                            loadMenuItemsByRestaurant(order.restaurantSlug());
                        } catch (SQLException e) {
                            showErrorAlert("Database Error", "Failed to load menu items: " + e.getMessage());
                        }
                        break;
                    }
                }
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
        
        // Set up display format for address combo boxes
        setupAddressComboBoxDisplayFormat(addressComboBox);
        setupAddressComboBoxDisplayFormat(newAddressComboBox);
        
        // Disable add item button initially
        addItemToOrderButton.setDisable(true);
        
        // Enable add item button when order is selected and menu item is selected
        menuItemsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            addItemToOrderButton.setDisable(newVal == null || selectedOrderCode == null);
        });
    }
    
    private void setupAddressComboBoxDisplayFormat(ComboBox<Address> comboBox) {
        comboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Address>() {
            @Override
            protected void updateItem(Address item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullAddress());
                }
            }
        });
        
        comboBox.setButtonCell(new javafx.scene.control.ListCell<Address>() {
            @Override
            protected void updateItem(Address item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullAddress());
                }
            }
        });
    }
    
    private void loadOrders() {
        try {
            orderViewModel.loadOrders();
            ordersTableView.setItems(orderViewModel.getOrders());
            
            // Update the orderForItemsComboBox
            orderForItemsComboBox.getItems().clear();
            for (Order order : orderViewModel.getOrders()) {
                orderForItemsComboBox.getItems().add(order.orderCode());
            }
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
            
            // Display address if available
            if (order.addressId() > 0) {
                for (Address address : addressComboBox.getItems()) {
                    if (address.id() == order.addressId()) {
                        addressInfoLabel.setText("Delivery Address: " + address.getFullAddress());
                        break;
                    }
                }
            } else {
                addressInfoLabel.setText("Delivery Address: Not set");
            }
            
            orderDetailsLabel.setText("Order #" + order.orderCode() + " - " + order.status());
            promoCodeLabel.setText("Discount: $" + order.discountAmount().toString());
        }
    }
    
    private void clearOrderDetails() {
        customerInfoLabel.setText("Customer: ");
        restaurantInfoLabel.setText("Restaurant: ");
        addressInfoLabel.setText("Delivery Address: ");
        orderDetailsLabel.setText("Order Details");
        promoCodeLabel.setText("Discount: $0.00");
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
                    selectedOrder.discountAmount(),
                    selectedOrder.addressId(),
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
                // Get the new values
                Address selectedAddress = addressComboBox.getValue();
                String newSpecialInstructions = specialInstructionsField.getText();
                String promoCode = promoCodeField.getText().trim();
                BigDecimal discountAmount = selectedOrder.discountAmount();
                
                // Apply promo code if it changed
                if (!promoCode.isEmpty()) {
                    try {
                        // Calculate the new discount amount from the promo code for an existing order
                        discountAmount = orderViewModel.calculateDiscountFromPromoCode(promoCode, selectedOrder.orderCode());
                        
                        // Record the promotion usage
                        orderViewModel.recordPromotionUsage(selectedOrder.orderCode(), promoCode);
                    } catch (IllegalStateException e) {
                        showErrorAlert("Promotion Error", e.getMessage());
                        return;
                    }
                }
                
                if (selectedAddress == null) {
                    showErrorAlert("Validation Error", "Please select a delivery address");
                    return;
                }
                
                // Get the new rider
                Rider selectedRider = riderComboBox.getValue();
                String newRiderId = selectedRider != null ? selectedRider.id() : null;
                
                // Create updated order with new values
                Order updatedOrder = new Order(
                    selectedOrder.orderCode(),
                    selectedOrder.customerUsername(),
                    selectedOrder.restaurantSlug(),
                    selectedOrder.status(),
                    discountAmount,
                    selectedAddress.id(),
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
    
    @FXML
    private void handleAddPayment() {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                // Validate payment fields
                if (paymentAmountField.getText().trim().isEmpty() ||
                    paymentMethodComboBox.getValue() == null) {
                    showErrorAlert("Validation Error", "Please enter payment amount and select payment method");
                    return;
                }
                
                // Parse payment amount
                BigDecimal amount;
                try {
                    amount = new BigDecimal(paymentAmountField.getText().trim());
                } catch (NumberFormatException e) {
                    showErrorAlert("Validation Error", "Please enter a valid payment amount");
                    return;
                }
                
                String paymentMethod = paymentMethodComboBox.getValue();
                String transactionId = transactionIdField.getText().trim();
                
                // Add payment to database
                orderViewModel.addPayment(selectedOrder.orderCode(), amount, paymentMethod, transactionId);
                
                // Clear payment fields
                paymentAmountField.clear();
                paymentMethodComboBox.setValue(null);
                transactionIdField.clear();
                
                showInfoAlert("Success", "Payment added successfully");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to add payment: " + e.getMessage());
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
    private void handleCustomerSelected() {
        String customerUsername = customerComboBox.getValue();
        if (customerUsername != null) {
            try {
                loadCustomerAddresses(customerUsername);
                
                // Update the new address combo box
                newAddressComboBox.setItems(FXCollections.observableArrayList(
                    orderViewModel.getCustomerAddresses()));
                
                if (!newAddressComboBox.getItems().isEmpty()) {
                    newAddressComboBox.setValue(newAddressComboBox.getItems().get(0));
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to load customer addresses: " + e.getMessage());
            }
        } else {
            newAddressComboBox.getItems().clear();
        }
    }
    
    @FXML
    private void handleRestaurantSelected() {
        String restaurantSlug = restaurantComboBox.getValue();
        if (restaurantSlug != null) {
            try {
                loadMenuItemsByRestaurant(restaurantSlug);
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to load menu items: " + e.getMessage());
            }
        } else {
            menuItemsTableView.getItems().clear();
        }
    }
    
    private void loadCustomerAddresses(String customerUsername) throws SQLException {
        // Check if we have the addresses cached
        if (!customerAddressesCache.containsKey(customerUsername)) {
            orderViewModel.loadCustomerAddresses(customerUsername);
            customerAddressesCache.put(customerUsername, 
                FXCollections.observableArrayList(orderViewModel.getCustomerAddresses()));
        } else {
            orderViewModel.getCustomerAddresses().clear();
            orderViewModel.getCustomerAddresses().addAll(customerAddressesCache.get(customerUsername));
        }
        
        // Update the address combo box
        addressComboBox.setItems(FXCollections.observableArrayList(
            orderViewModel.getCustomerAddresses()));
    }
    
    private void loadMenuItemsByRestaurant(String restaurantSlug) throws SQLException {
        orderViewModel.loadMenuItemsByRestaurant(restaurantSlug);
        menuItemsTableView.setItems(orderViewModel.getMenuItems());
    }
    
    @FXML
    private void handleCreateOrder() {
        // Validate form fields
        if (customerComboBox.getValue() == null ||
            restaurantComboBox.getValue() == null ||
            newAddressComboBox.getValue() == null) {
            
            showErrorAlert("Validation Error", "Please fill in all required fields");
            return;
        }
        
        try {
            // Generate order code automatically
            String orderCode = orderViewModel.generateOrderCode();
            
            // Create a new order
            String customerUsername = customerComboBox.getValue();
            String restaurantSlug = restaurantComboBox.getValue();
            Address selectedAddress = newAddressComboBox.getValue();
            String specialInstructions = newSpecialInstructionsField.getText().trim();
            String promoCode = newPromoCodeField.getText().trim();
            BigDecimal discountAmount = BigDecimal.ZERO;
            
            // Calculate discount if promo code is provided
            if (!promoCode.isEmpty()) {
                try {
                    // Try to apply the promotion and get the discount amount for a new order
                    // Pass null as order code since this is a new order
                    discountAmount = orderViewModel.calculateDiscountFromPromoCode(promoCode, null);
                } catch (IllegalStateException e) {
                    showErrorAlert("Promotion Error", e.getMessage());
                    return;
                }
            }
            
            // Default to PENDING status
            OrderStatus status = OrderStatus.PENDING;
            LocalDateTime now = LocalDateTime.now();
            
            // Create initial order with no items
            Order newOrder = new Order(
                orderCode,
                customerUsername,
                restaurantSlug,
                status,  // Default status is PENDING
                discountAmount,  // Calculated discount amount from promo code
                selectedAddress.id(),
                specialInstructions,
                null,                              // No rider assigned yet
                List.of(),                         // Empty items list
                now,                               // placed at
                now                                // updated at
            );
            
            // Save the order
            orderViewModel.createOrder(newOrder);
            
            // Record promotion usage if applicable
            if (!promoCode.isEmpty()) {
                orderViewModel.recordPromotionUsage(orderCode, promoCode);
            }
            
            // Refresh the orders list
            loadOrders();
            
            // Add the new order to the orderForItemsComboBox and select it
            orderForItemsComboBox.getItems().add(orderCode);
            orderForItemsComboBox.setValue(orderCode);
            selectedOrderCode = orderCode;
            
            // Clear the form but keep selected customer and restaurant
            newSpecialInstructionsField.clear();
            newPromoCodeField.clear();
            
            showInfoAlert("Success", "Order #" + orderCode + " created successfully. Add items to the order.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to create order: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddItemToOrder() {
        if (selectedOrderCode == null) {
            showErrorAlert("Selection Error", "Please select an order first");
            return;
        }
        
        MenuItem selectedMenuItem = menuItemsTableView.getSelectionModel().getSelectedItem();
        if (selectedMenuItem == null) {
            showErrorAlert("Selection Error", "Please select a menu item");
            return;
        }
        
        // Get quantity and special instructions
        int quantity = itemQuantitySpinner.getValue();
        String specialInstructions = itemSpecialInstructionsField.getText().trim();
        
        try {
            // Add the item to the order
            orderViewModel.addOrderItem(selectedOrderCode, selectedMenuItem, quantity, specialInstructions);
            
            // Refresh the order items list if this is the currently selected order
            Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
            if (selectedOrder != null && selectedOrder.orderCode().equals(selectedOrderCode)) {
                displayOrderDetails(selectedOrderCode);
            }
            
            // Recalculate and update the order total
            orderViewModel.recalculateOrderTotal(selectedOrderCode);
            
            // Clear fields
            itemSpecialInstructionsField.clear();
            itemQuantitySpinner.getValueFactory().setValue(1);
            
            showInfoAlert("Success", "Item added to order");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add item to order: " + e.getMessage());
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