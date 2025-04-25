package com.foodapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the Riders view
 */
public class RidersController {

    @FXML
    private TableView<?> ridersTableView;
    
    @FXML
    private TableColumn<?, ?> riderIdColumn;
    
    @FXML
    private TableColumn<?, ?> riderNameColumn;
    
    @FXML
    private TableColumn<?, ?> riderPhoneColumn;
    
    @FXML
    private TableColumn<?, ?> riderEmailColumn;
    
    @FXML
    private TableColumn<?, ?> riderStatusColumn;
    
    @FXML
    private TableColumn<?, ?> riderVehicleColumn;
    
    @FXML
    private TableView<?> vehiclesTableView;
    
    @FXML
    private TableColumn<?, ?> registrationColumn;
    
    @FXML
    private TableColumn<?, ?> typeColumn;
    
    @FXML
    private TableColumn<?, ?> makeModelColumn;
    
    @FXML
    private TableColumn<?, ?> yearColumn;
    
    @FXML
    private TableColumn<?, ?> colorColumn;
    
    @FXML
    private TableColumn<?, ?> statusColumn;
    
    @FXML
    private TableColumn<?, ?> assignedToColumn;
    
    @FXML
    private void initialize() {
        // Initialize controller
    }
} 