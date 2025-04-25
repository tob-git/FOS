package com.foodapp.controller;

import com.foodapp.dao.PromotionDAO;
import com.foodapp.model.Promotion;
import com.foodapp.model.Promotion.PromotionStatus;
import com.foodapp.model.Promotion.PromotionType;
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
    
    private ObservableList<Promotion> promotions = FXCollections.observableArrayList();
    private final PromotionDAO promotionDAO;
    
    public PromotionsController() {
        this.promotionDAO = new PromotionDAO();
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
        
        // Initialize buttons
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // Load data from database
        loadPromotions();
    }
    
    private void loadPromotions() {
        try {
            List<Promotion> promotionsList = promotionDAO.findAll();
            promotions.clear();
            promotions.addAll(promotionsList);
            promotionsTableView.setItems(promotions);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load promotions: " + e.getMessage());
        }
    }
    
    private void fillFormWithPromotion(Promotion promotion) {
        codeField.setText(promotion.code());
        descriptionField.setText(promotion.description());
        discountField.setText(promotion.value().toString());
        validFromPicker.setValue(promotion.startDate().toLocalDate());
        validToPicker.setValue(promotion.endDate().toLocalDate());
    }
    
    private void clearForm() {
        codeField.clear();
        descriptionField.clear();
        discountField.clear();
        validFromPicker.setValue(null);
        validToPicker.setValue(null);
    }
    
    @FXML
    private void handleAdd() {
        if (validateForm()) {
            try {
                Promotion newPromotion = createPromotionFromForm();
                promotionDAO.insert(newPromotion);
                
                // Refresh the table
                loadPromotions();
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
                promotionDAO.update(updatedPromotion);
                
                // Refresh the table
                loadPromotions();
                
                // Re-select the updated promotion
                for (Promotion promotion : promotions) {
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
                promotionDAO.delete(selectedPromotion.code());
                
                // Refresh the table
                loadPromotions();
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
                List<Promotion> searchResults = promotionDAO.search(searchText);
                promotions.clear();
                promotions.addAll(searchResults);
                promotionsTableView.setItems(promotions);
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
        
        if (code.isEmpty() || description.isEmpty() || discountText.isEmpty() 
                || validFrom == null || validTo == null) {
            showErrorAlert("Validation Error", "All fields are required");
            return false;
        }
        
        try {
            BigDecimal discount = new BigDecimal(discountText);
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                showErrorAlert("Validation Error", "Discount cannot be negative");
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
        
        return true;
    }
    
    private Promotion createPromotionFromForm() {
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        BigDecimal value = new BigDecimal(discountField.getText().trim());
        
        LocalDateTime startDate = validFromPicker.getValue().atStartOfDay();
        LocalDateTime endDate = validToPicker.getValue().atTime(23, 59, 59);
        
        // Determine if this is a percentage or fixed amount discount
        PromotionType type = PromotionType.FIXED_AMOUNT;  // Default
        
        // Set status based on dates
        PromotionStatus status = PromotionStatus.ACTIVE;
        
        // Default values for other fields
        String restaurantSlug = "";  // This might need to be properly set
        int usageLimit = 0;          // Unlimited by default
        int usageCount = 0;          // New promotion starts with 0 uses
        LocalDateTime now = LocalDateTime.now();
        
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