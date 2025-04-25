package com.foodapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;
    
    @FXML
    private Label userLabel;

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            contentArea.getChildren().clear();
            Label errorLabel = new Label("Error loading view: " + e.getMessage());
            errorLabel.getStyleClass().add("error-message");
            contentArea.getChildren().add(errorLabel);
        }
    }
    
    @FXML
    private void showCustomersView() {
        loadView("/com/foodapp/view/CustomersView.fxml");
    }
    
    @FXML
    private void showRestaurantsView() {
        loadView("/com/foodapp/view/RestaurantsView.fxml");
    }
    
    @FXML
    private void showOrdersView() {
        loadView("/com/foodapp/view/OrdersView.fxml");
    }
    
    @FXML
    private void showPromotionsView() {
        loadView("/com/foodapp/view/PromotionsView.fxml");
    }
    
    @FXML
    private void showRidersView() {
        loadView("/com/foodapp/view/RidersView.fxml");
    }
    
    public void initialize() {
        // Set default view or perform other initialization
        userLabel.setText("Welcome, Admin");
    }
} 