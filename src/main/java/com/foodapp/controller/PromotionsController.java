package com.foodapp.controller;

import com.foodapp.model.Promotion;
import com.foodapp.model.Promotion.PromotionStatus;
import com.foodapp.model.Promotion.PromotionType;
import com.foodapp.viewmodel.PromotionViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Promotions view
 */
public class PromotionsController {

    @FXML
    private TableView<Promotion> promotionsTableView;
    
    @FXML
    private TableColumn<Promotion, String> codeColumn;
    
    @FXML
    private TableColumn<Promotion, String> descriptionColumn;
    
    @FXML
    private TableColumn<Promotion, String> discountColumn;
    
    @FXML
    private TableColumn<Promotion, String> validFromColumn;
    
    @FXML
    private TableColumn<Promotion, String> validToColumn;
    
    @FXML
    private TableColumn<Promotion, Boolean> activeColumn;
    
    @FXML
    private TextField codeField;
    
    @FXML
    private TextField descriptionField;
    
    @FXML
    private TextField discountField;
    
    @FXML
    private DatePicker validFromPicker;
    
    @FXML
    private DatePicker validToPicker;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private ComboBox<PromotionType> promotionTypeComboBox;
    
    @FXML
    private TextField usageLimitField;
    
    @FXML
    private ComboBox<String> restaurantComboBox;
    
    private final PromotionViewModel promotionViewModel;
    
    public PromotionsController() {
        this.promotionViewModel = new PromotionViewModel();
    }
    
    @FXML
    private void initialize() {
        // Initialize table columns
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        discountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDiscount()));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        validFromColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().startDate().format(formatter)));
        validToColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().endDate().format(formatter)));
        
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        // Setup selection listener for promotions table
        promotionsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFormWithPromotion(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                clearForm();
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Initialize promotion type combo box
        promotionTypeComboBox.setItems(FXCollections.observableArrayList(PromotionType.values()));
        
        // Load restaurants for dropdown
        loadRestaurants();
        
        // Initialize buttons
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // Load data from database
        loadPromotions();
    }
    
    private void loadPromotions() {
        try {
            promotionViewModel.loadPromotions();
            promotionsTableView.setItems(promotionViewModel.getPromotions());
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load promotions: " + e.getMessage());
        }
    }
    
    private void loadRestaurants() {
        try {
            List<String> restaurantSlugs = promotionViewModel.loadRestaurantSlugs();
            restaurantComboBox.setItems(FXCollections.observableArrayList(restaurantSlugs));
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load restaurants: " + e.getMessage());
        }
    }
    
    private void fillFormWithPromotion(Promotion promotion) {
        codeField.setText(promotion.code());
        descriptionField.setText(promotion.description());
        discountField.setText(promotion.value().toString());
        validFromPicker.setValue(promotion.startDate().toLocalDate());
        validToPicker.setValue(promotion.endDate().toLocalDate());
        promotionTypeComboBox.setValue(promotion.type());
        usageLimitField.setText(String.valueOf(promotion.usageLimit()));
        restaurantComboBox.setValue(promotion.restaurantSlug());
    }
    
    private void clearForm() {
        codeField.clear();
        descriptionField.clear();
        discountField.clear();
        validFromPicker.setValue(null);
        validToPicker.setValue(null);
        promotionTypeComboBox.setValue(null);
        usageLimitField.clear();
        restaurantComboBox.setValue(null);
    }
    
    @FXML
    private void handleAdd() {
        if (validateForm()) {
            try {
                Promotion newPromotion = createPromotionFromForm();
                promotionViewModel.addPromotion(newPromotion);
                promotionsTableView.setItems(promotionViewModel.getPromotions());
                clearForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to add promotion: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleUpdate() {
        Promotion selectedPromotion = promotionsTableView.getSelectionModel().getSelectedItem();
        if (selectedPromotion != null && validateForm()) {
            try {
                Promotion updatedPromotion = createPromotionFromForm();
                promotionViewModel.updatePromotion(updatedPromotion);
                promotionsTableView.setItems(promotionViewModel.getPromotions());
                
                // Re-select the updated promotion
                for (Promotion promotion : promotionViewModel.getPromotions()) {
                    if (promotion.code().equals(updatedPromotion.code())) {
                        promotionsTableView.getSelectionModel().select(promotion);
                        break;
                    }
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update promotion: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDelete() {
        Promotion selectedPromotion = promotionsTableView.getSelectionModel().getSelectedItem();
        if (selectedPromotion != null) {
            try {
                promotionViewModel.deletePromotion(selectedPromotion.code());
                promotionsTableView.setItems(promotionViewModel.getPromotions());
                clearForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete promotion: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadPromotions();
        } else {
            try {
                promotionViewModel.searchPromotions(searchText);
                promotionsTableView.setItems(promotionViewModel.getPromotions());
            } catch (SQLException e) {
                showErrorAlert("Search Error", "Failed to search promotions: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadPromotions();
        clearForm();
    }
    
    private boolean validateForm() {
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        String discountText = discountField.getText().trim();
        LocalDate validFrom = validFromPicker.getValue();
        LocalDate validTo = validToPicker.getValue();
        PromotionType type = promotionTypeComboBox.getValue();
        
        if (code.isEmpty() || description.isEmpty() || discountText.isEmpty() 
                || validFrom == null || validTo == null || type == null) {
            showErrorAlert("Validation Error", "All fields are required");
            return false;
        }
        
        try {
            BigDecimal discount = new BigDecimal(discountText);
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                showErrorAlert("Validation Error", "Discount cannot be negative");
                return false;
            }
            
            // Validate percentage values
            if (type == PromotionType.PERCENTAGE && discount.compareTo(new BigDecimal("100")) > 0) {
                showErrorAlert("Validation Error", "Percentage discount cannot exceed 100%");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Discount must be a valid number");
            return false;
        }
        
        if (validFrom.isAfter(validTo)) {
            showErrorAlert("Validation Error", "Valid From date must be before Valid To date");
            return false;
        }
        
        // Validate usage limit
        if (!usageLimitField.getText().trim().isEmpty()) {
            try {
                int usageLimit = Integer.parseInt(usageLimitField.getText().trim());
                if (usageLimit < 0) {
                    showErrorAlert("Validation Error", "Usage limit cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Validation Error", "Usage limit must be a valid number");
                return false;
            }
        }
        
        return true;
    }
    
    private Promotion createPromotionFromForm() {
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        BigDecimal value = new BigDecimal(discountField.getText().trim());
        
        LocalDateTime startDate = validFromPicker.getValue().atStartOfDay();
        LocalDateTime endDate = validToPicker.getValue().atTime(23, 59, 59);
        
        // Get promotion type
        PromotionType type = promotionTypeComboBox.getValue();
        
        // Set status based on dates
        PromotionStatus status = PromotionStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();
        if (startDate.isAfter(now)) {
            status = PromotionStatus.ACTIVE; // Future promotion, still mark as ACTIVE
        } else if (endDate.isBefore(now)) {
            status = PromotionStatus.EXPIRED;
        }
        
        // Get restaurant slug
        String restaurantSlug = restaurantComboBox.getValue();
        
        // Get usage limit
        int usageLimit = 0;
        if (!usageLimitField.getText().trim().isEmpty()) {
            usageLimit = Integer.parseInt(usageLimitField.getText().trim());
        }
        
        // Default values for other fields
        int usageCount = 0;    // New promotion starts with 0 uses
        
        return new Promotion(
            code,
            description,
            type,
            value,
            startDate,
            endDate,
            status,
            restaurantSlug,
            usageLimit,
            usageCount,
            now,  // createdAt
            now   // updatedAt
        );
    }
    
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 