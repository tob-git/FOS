package com.foodapp.controller;

import com.foodapp.model.Address;
import com.foodapp.model.Customer;
import com.foodapp.model.Customer.CustomerStatus;
import com.foodapp.viewmodel.CustomerViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
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
    
    // Address UI components
    @FXML
    private TableView<Address> addressTable;
    
    @FXML
    private TableColumn<Address, String> streetColumn;
    
    @FXML
    private TableColumn<Address, String> cityColumn;
    
    @FXML
    private TableColumn<Address, String> stateColumn;
    
    @FXML
    private TableColumn<Address, String> postalCodeColumn;
    
    @FXML
    private TableColumn<Address, String> countryColumn;
    
    @FXML
    private TextField streetField;
    
    @FXML
    private TextField cityField;
    
    @FXML
    private TextField stateField;
    
    @FXML
    private TextField postalCodeField;
    
    @FXML
    private TextField countryField;
    
    @FXML
    private Button addAddressButton;
    
    @FXML
    private Button saveAddressButton;
    
    @FXML
    private Button deleteAddressButton;
    
    private CustomerViewModel viewModel;
    private final BooleanProperty noCustomerSelected = new SimpleBooleanProperty(true);
    
    @FXML
    private void initialize() {
        viewModel = new CustomerViewModel();
        
        // Configure customer table columns
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
        
        // Configure address table columns
        streetColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().street()));
        cityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().city()));
        stateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().state()));
        postalCodeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().postalCode()));
        countryColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().country()));
        
        // Address table selection listener
        addressTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                viewModel.selectAddress(newSelection);
                deleteAddressButton.setDisable(false);
            } else {
                viewModel.selectAddress(null);
                deleteAddressButton.setDisable(true);
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
        
        // Bind address fields
        addressTable.setItems(viewModel.getAddressList());
        streetField.textProperty().bindBidirectional(viewModel.streetProperty());
        cityField.textProperty().bindBidirectional(viewModel.cityProperty());
        stateField.textProperty().bindBidirectional(viewModel.stateProperty());
        postalCodeField.textProperty().bindBidirectional(viewModel.postalCodeProperty());
        countryField.textProperty().bindBidirectional(viewModel.countryProperty());
        
        // Set up noCustomerSelected property
        noCustomerSelected.bind(Bindings.isNull(viewModel.selectedCustomerProperty()));
        
        // Bind button states
        addAddressButton.disableProperty().bind(noCustomerSelected);
        saveAddressButton.disableProperty().bind(noCustomerSelected);
        
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
    
    @FXML
    private void handleNewAddress() {
        viewModel.clearAddressForm();
    }
    
    @FXML
    private void handleSaveAddress() {
        // Validate address form
        if (!validateAddressForm()) {
            return;
        }
        
        try {
            viewModel.saveAddress();
        } catch (SQLException e) {
            showErrorAlert("Failed to save address", e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteAddress() {
        Address selectedAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedAddress != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Address");
            confirmation.setContentText("Are you sure you want to delete this address?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    viewModel.deleteAddress();
                } catch (SQLException e) {
                    showErrorAlert("Failed to delete address", e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleClearAddress() {
        viewModel.clearAddressForm();
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
    
    private boolean validateAddressForm() {
        StringBuilder errors = new StringBuilder();
        
        if (streetField.getText().isBlank()) {
            errors.append("Street is required\n");
        }
        
        if (cityField.getText().isBlank()) {
            errors.append("City is required\n");
        }
        
        if (stateField.getText().isBlank()) {
            errors.append("State is required\n");
        }
        
        if (postalCodeField.getText().isBlank()) {
            errors.append("Postal code is required\n");
        }
        
        if (countryField.getText().isBlank()) {
            errors.append("Country is required\n");
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
    
    // Getter for binding in FXML
    public BooleanProperty noCustomerSelectedProperty() {
        return noCustomerSelected;
    }
} 