package com.foodapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the Orders view
 */
public class OrdersController {

    @FXML
    private TableView<?> ordersTableView;
    
    @FXML
    private TableColumn<?, ?> orderCodeColumn;
    
    @FXML
    private TableColumn<?, ?> customerColumn;
    
    @FXML
    private TableColumn<?, ?> restaurantColumn;
    
    @FXML
    private TableColumn<?, ?> statusColumn;
    
    @FXML
    private TableColumn<?, ?> totalColumn;
    
    @FXML
    private TableColumn<?, ?> dateColumn;
    
    @FXML
    private TableView<?> orderItemsTableView;
    
    @FXML
    private TableColumn<?, ?> itemNameColumn;
    
    @FXML
    private TableColumn<?, ?> quantityColumn;
    
    @FXML
    private TableColumn<?, ?> priceColumn;
    
    @FXML
    private TableColumn<?, ?> subtotalColumn;
    
    @FXML
    private void initialize() {
        // Initialize controller
    }
} 