package com.foodapp.controller;

import com.foodapp.model.Customer;
import com.foodapp.model.Customer.CustomerStatus;
import com.foodapp.viewmodel.CustomerViewModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.Optional;

public class CustomersController {

    @FXML
    private TableView<Customer> customerTable;
    
    @FXML
    private TableColumn<Customer, String> usernameColumn;
    
    @FXML
    private TableColumn<Customer, String> emailColumn;
    
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    
    @FXML
    private TableColumn<Customer, String> nameColumn;
    
    @FXML
    private TableColumn<Customer, CustomerStatus> statusColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private ComboBox<CustomerStatus> statusComboBox;
    
    private CustomerViewModel viewModel;
    
    @FXML
    private void initialize() {
        viewModel = new CustomerViewModel();
        
        // Configure table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        nameColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(customer.getFullName());
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status cell formatting
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(CustomerStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-active", "status-inactive", "status-blocked");
                } else {
                    setText(status.toString());
                    getStyleClass().removeAll("status-active", "status-inactive", "status-blocked");
                    
                    switch (status) {
                        case ACTIVE -> getStyleClass().add("status-active");
                        case INACTIVE -> getStyleClass().add("status-inactive");
                        case BLOCKED -> getStyleClass().add("status-blocked");
                    }
                }
            }
        });
        
        // Set up ComboBox
        statusComboBox.setItems(FXCollections.observableArrayList(CustomerStatus.values()));
        statusComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(CustomerStatus status) {
                return status == null ? "" : status.name();
            }
            
            @Override
            public CustomerStatus fromString(String string) {
                return string == null || string.isEmpty() 
                        ? null 
                        : CustomerStatus.valueOf(string);
            }
        });
        
        // Bind to view model
        customerTable.setItems(viewModel.getCustomerList());
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());
        
        usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        phoneField.textProperty().bindBidirectional(viewModel.phoneProperty());
        firstNameField.textProperty().bindBidirectional(viewModel.firstNameProperty());
        lastNameField.textProperty().bindBidirectional(viewModel.lastNameProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        statusComboBox.valueProperty().bindBidirectional(viewModel.statusProperty());
        
        // Set up table selection listener
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
            
            if (newSelection != null) {
                viewModel.selectCustomer(newSelection);
                usernameField.setEditable(false);
            }
        });
        
        // Load initial data
        viewModel.loadAllCustomers();
    }
    
    @FXML
    private void handleSearch() {
        viewModel.searchCustomers();
    }
    
    @FXML
    private void handleNewCustomer() {
        viewModel.clearForm();
        usernameField.setEditable(true);
    }
    
    @FXML
    private void handleEditCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            viewModel.selectCustomer(selectedCustomer);
            usernameField.setEditable(false);
        }
    }
    
    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Customer");
            confirmation.setContentText("Are you sure you want to delete customer: " + selectedCustomer.username() + "?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    viewModel.deleteCustomer();
                } catch (SQLException e) {
                    showErrorAlert("Failed to delete customer", e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        viewModel.loadAllCustomers();
    }
    
    @FXML
    private void handleClear() {
        viewModel.clearForm();
        usernameField.setEditable(true);
    }
    
    @FXML
    private void handleSave() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        try {
            viewModel.saveCustomer();
            usernameField.setEditable(true);
        } catch (SQLException e) {
            showErrorAlert("Failed to save customer", e.getMessage());
        }
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (usernameField.getText().isBlank()) {
            errors.append("Username is required\n");
        }
        
        if (emailField.getText().isBlank()) {
            errors.append("Email is required\n");
        } else if (!isValidEmail(emailField.getText())) {
            errors.append("Invalid email format\n");
        }
        
        if (phoneField.getText().isBlank()) {
            errors.append("Phone is required\n");
        }
        
        if (firstNameField.getText().isBlank()) {
            errors.append("First name is required\n");
        }
        
        if (lastNameField.getText().isBlank()) {
            errors.append("Last name is required\n");
        }
        
        if (passwordField.getText().isBlank() && !viewModel.selectedCustomerProperty().isNotNull().get()) {
            errors.append("Password is required for new customers\n");
        }
        
        if (statusComboBox.getValue() == null) {
            errors.append("Status is required\n");
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
    
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 