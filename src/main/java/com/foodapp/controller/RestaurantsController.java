package com.foodapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * Controller for the Restaurants view
 */
public class RestaurantsController {

    @FXML
    private ListView<String> restaurantListView;
    
    @FXML
    private TreeTableView<?> menuTreeTableView;
    
    @FXML
    private TreeTableColumn<?, ?> itemNameColumn;
    
    @FXML
    private TreeTableColumn<?, ?> itemDescriptionColumn;
    
    @FXML
    private TreeTableColumn<?, ?> itemPriceColumn;
    
    @FXML
    private TreeTableColumn<?, ?> itemCategoryColumn;
    
    @FXML
    private TreeTableColumn<?, ?> itemAvailableColumn;
    
    @FXML
    private void initialize() {
        // Initialize with some dummy data for now
        restaurantListView.getItems().addAll(
            "NYC Pizza",
            "Burger King",
            "Taco Bell",
            "Sushi Express"
        );
    }
} 