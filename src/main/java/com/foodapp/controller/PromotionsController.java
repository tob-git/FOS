package com.foodapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the Promotions view
 */
public class PromotionsController {

    @FXML
    private TableView<?> promotionsTableView;
    
    @FXML
    private TableColumn<?, ?> codeColumn;
    
    @FXML
    private TableColumn<?, ?> descriptionColumn;
    
    @FXML
    private TableColumn<?, ?> typeColumn;
    
    @FXML
    private TableColumn<?, ?> valueColumn;
    
    @FXML
    private TableColumn<?, ?> startDateColumn;
    
    @FXML
    private TableColumn<?, ?> endDateColumn;
    
    @FXML
    private TableColumn<?, ?> statusColumn;
    
    @FXML
    private TableColumn<?, ?> usageColumn;
    
    @FXML
    private void initialize() {
        // Initialize controller
    }
} 