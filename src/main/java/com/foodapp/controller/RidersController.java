package com.foodapp.controller;

import com.foodapp.model.Rider;
import com.foodapp.model.Rider.RiderStatus;
import com.foodapp.model.Vehicle;
import com.foodapp.model.Vehicle.VehicleStatus;
import com.foodapp.model.Vehicle.VehicleType;
import com.foodapp.viewmodel.RiderViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Riders view
 */
public class RidersController {

    @FXML
    private TableView<Rider> ridersTableView;
    
    @FXML
    private TableColumn<Rider, String> riderIdColumn;
    
    @FXML
    private TableColumn<Rider, String> riderNameColumn;
    
    @FXML
    private TableColumn<Rider, String> riderPhoneColumn;
    
    @FXML
    private TableColumn<Rider, String> riderEmailColumn;
    
    @FXML
    private TableColumn<Rider, RiderStatus> riderStatusColumn;
    
    @FXML
    private TableView<Vehicle> vehiclesTableView;
    
    @FXML
    private TableColumn<Vehicle, String> registrationColumn;
    
    @FXML
    private TableColumn<Vehicle, VehicleType> typeColumn;
    
    @FXML
    private TableColumn<Vehicle, String> makeModelColumn;
    
    @FXML
    private TableColumn<Vehicle, Integer> yearColumn;
    
    @FXML
    private TableColumn<Vehicle, String> colorColumn;
    
    @FXML
    private TableColumn<Vehicle, VehicleStatus> statusColumn;
    
    @FXML
    private TableColumn<Vehicle, String> assignedToColumn;
    
    // Form fields for riders
    @FXML
    private TextField riderIdField;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private ComboBox<RiderStatus> statusComboBox;
    
    @FXML
    private DatePicker dobPicker;
    
    @FXML
    private TextField licenseNumberField;
    
    @FXML
    private DatePicker licenseExpiryPicker;
    
    // Form fields for vehicles
    @FXML
    private TextField registrationField;
    
    @FXML
    private ComboBox<VehicleType> vehicleTypeComboBox;
    
    @FXML
    private TextField makeField;
    
    @FXML
    private TextField modelField;
    
    @FXML
    private TextField yearField;
    
    @FXML
    private TextField colorField;
    
    @FXML
    private ComboBox<VehicleStatus> vehicleStatusComboBox;
    
    @FXML
    private DatePicker insuranceExpiryPicker;
    
    @FXML
    private ComboBox<Rider> riderComboBox;
    
    // Buttons
    @FXML
    private Button saveRiderButton;
    
    @FXML
    private Button deleteRiderButton;
    
    @FXML
    private Button saveVehicleButton;
    
    @FXML
    private Button deleteVehicleButton;
    
    @FXML
    private TextField searchField;
    
    private final RiderViewModel riderViewModel;
    private Rider selectedRider;
    private Vehicle selectedVehicle;
    
    public RidersController() {
        this.riderViewModel = new RiderViewModel();
    }
    
    @FXML
    private void initialize() {
        // Configure rider table columns using lambda expressions for record fields
        riderIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().id()));
        
        riderNameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFullName()));
        
        riderPhoneColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().phone()));
            
        riderEmailColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().email()));
            
        riderStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().status()));
        
        // Configure vehicle table columns using lambda expressions for record fields
        registrationColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().registrationNumber()));
            
        typeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().type()));
        
        makeModelColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().make() + " " + data.getValue().model()));
        
        yearColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().yearOfManufacture()).asObject());
            
        colorColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().color()));
            
        statusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().status()));
        
        assignedToColumn.setCellValueFactory(data -> {
            Vehicle vehicle = data.getValue();
            if (vehicle.isAssigned()) {
                for (Rider rider : riderViewModel.getRiders()) {
                    if (rider.id().equals(vehicle.riderId())) {
                        return new SimpleStringProperty(rider.getFullName());
                    }
                }
                return new SimpleStringProperty("Unknown rider: " + vehicle.riderId());
            } else {
                return new SimpleStringProperty("Unassigned");
            }
        });
        
        // Status cell coloring for riders
        riderStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(RiderStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    switch (status) {
                        case ACTIVE -> setStyle("-fx-text-fill: green;");
                        case INACTIVE -> setStyle("-fx-text-fill: gray;");
                        case ON_DELIVERY -> setStyle("-fx-text-fill: orange;");
                        case ON_BREAK -> setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        });
        
        // Status cell coloring for vehicles
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(VehicleStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    switch (status) {
                        case ACTIVE -> setStyle("-fx-text-fill: green;");
                        case INACTIVE -> setStyle("-fx-text-fill: gray;");
                        case MAINTENANCE -> setStyle("-fx-text-fill: orange;");
                    }
                }
            }
        });
        
        // Setup selection listeners
        ridersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRider = newSelection;
                populateRiderForm(newSelection);
                deleteRiderButton.setDisable(false);
            } else {
                selectedRider = null;
                clearRiderForm();
                deleteRiderButton.setDisable(true);
            }
        });
        
        vehiclesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedVehicle = newSelection;
                populateVehicleForm(newSelection);
                deleteVehicleButton.setDisable(false);
            } else {
                selectedVehicle = null;
                clearVehicleForm();
                deleteVehicleButton.setDisable(true);
            }
        });
        
        // Setup combo boxes
        statusComboBox.setItems(FXCollections.observableArrayList(RiderStatus.values()));
        vehicleTypeComboBox.setItems(FXCollections.observableArrayList(VehicleType.values()));
        vehicleStatusComboBox.setItems(FXCollections.observableArrayList(VehicleStatus.values()));
        
        // Load data from database
        loadData();
        
        // Setup rider combo box for vehicles
        updateRiderComboBox();
    }
    
    private void loadData() {
        try {
            riderViewModel.loadRiders();
            riderViewModel.loadVehicles();
            ridersTableView.setItems(riderViewModel.getRiders());
            vehiclesTableView.setItems(riderViewModel.getVehicles());
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load data: " + e.getMessage());
        }
    }
    
    private void populateRiderForm(Rider rider) {
        riderIdField.setText(rider.id());
        firstNameField.setText(rider.firstName());
        lastNameField.setText(rider.lastName());
        phoneField.setText(rider.phone());
        emailField.setText(rider.email());
        statusComboBox.setValue(rider.status());
        dobPicker.setValue(rider.dateOfBirth());
        licenseNumberField.setText(rider.licenseNumber());
        licenseExpiryPicker.setValue(rider.licenseExpiry());
    }
    
    private void populateVehicleForm(Vehicle vehicle) {
        registrationField.setText(vehicle.registrationNumber());
        vehicleTypeComboBox.setValue(vehicle.type());
        makeField.setText(vehicle.make());
        modelField.setText(vehicle.model());
        yearField.setText(String.valueOf(vehicle.yearOfManufacture()));
        colorField.setText(vehicle.color());
        vehicleStatusComboBox.setValue(vehicle.status());
        insuranceExpiryPicker.setValue(vehicle.insuranceExpiryDate());
        
        if (vehicle.isAssigned()) {
            for (Rider rider : riderViewModel.getRiders()) {
                if (rider.id().equals(vehicle.riderId())) {
                    riderComboBox.setValue(rider);
                    break;
                }
            }
        } else {
            riderComboBox.setValue(null);
        }
    }
    
    private void clearRiderForm() {
        riderIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
        statusComboBox.setValue(null);
        dobPicker.setValue(null);
        licenseNumberField.clear();
        licenseExpiryPicker.setValue(null);
    }
    
    private void clearVehicleForm() {
        registrationField.clear();
        vehicleTypeComboBox.setValue(null);
        makeField.clear();
        modelField.clear();
        yearField.clear();
        colorField.clear();
        vehicleStatusComboBox.setValue(null);
        insuranceExpiryPicker.setValue(null);
        riderComboBox.setValue(null);
    }
    
    private void updateRiderComboBox() {
        riderComboBox.setItems(riderViewModel.getRiders());
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
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadData();
        } else {
            try {
                riderViewModel.searchRiders(searchText);
                ridersTableView.setItems(riderViewModel.getRiders());
            } catch (SQLException e) {
                showErrorAlert("Search Error", "Failed to search riders: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSaveRider() {
        if (validateRiderForm()) {
            try {
                Rider rider = createRiderFromForm();
                if (selectedRider == null) {
                    riderViewModel.addRider(rider);
                } else {
                    riderViewModel.updateRider(rider);
                }
                loadData();
                clearRiderForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to save rider: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDeleteRider() {
        if (selectedRider != null) {
            try {
                riderViewModel.deleteRider(selectedRider.id());
                loadData();
                clearRiderForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete rider: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSaveVehicle() {
        if (validateVehicleForm()) {
            try {
                Vehicle vehicle = createVehicleFromForm();
                if (selectedVehicle == null) {
                    riderViewModel.addVehicle(vehicle);
                } else {
                    riderViewModel.updateVehicle(vehicle);
                }
                loadData();
                clearVehicleForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to save vehicle: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDeleteVehicle() {
        if (selectedVehicle != null) {
            try {
                riderViewModel.deleteVehicle(selectedVehicle.registrationNumber());
                loadData();
                clearVehicleForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete vehicle: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
    
    private boolean validateRiderForm() {
        // Validate required fields
        if (firstNameField.getText().trim().isEmpty() || 
            lastNameField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            statusComboBox.getValue() == null ||
            dobPicker.getValue() == null) {
            
            showErrorAlert("Validation Error", "Please fill in all required fields");
            return false;
        }
        
        // Validate email format
        if (!emailField.getText().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            showErrorAlert("Validation Error", "Please enter a valid email address");
            return false;
        }
        
        // Validate phone number (simple numeric check)
        if (!phoneField.getText().matches("^[0-9\\+\\-\\s]{10,15}$")) {
            showErrorAlert("Validation Error", "Please enter a valid phone number");
            return false;
        }
        
        // Validate age (must be at least 18 years old)
        LocalDate now = LocalDate.now();
        if (dobPicker.getValue().plusYears(18).isAfter(now)) {
            showErrorAlert("Validation Error", "Rider must be at least 18 years old");
            return false;
        }
        
        // Validate license expiry date
        if (licenseExpiryPicker.getValue() != null && licenseExpiryPicker.getValue().isBefore(now)) {
            showErrorAlert("Validation Error", "License expiry date cannot be in the past");
            return false;
        }
        
        return true;
    }
    
    private boolean validateVehicleForm() {
        // Validate required fields
        if (registrationField.getText().trim().isEmpty() || 
            vehicleTypeComboBox.getValue() == null ||
            makeField.getText().trim().isEmpty() ||
            modelField.getText().trim().isEmpty() ||
            yearField.getText().trim().isEmpty() ||
            colorField.getText().trim().isEmpty() ||
            vehicleStatusComboBox.getValue() == null ||
            insuranceExpiryPicker.getValue() == null) {
            
            showErrorAlert("Validation Error", "Please fill in all required fields");
            return false;
        }
        
        // Validate registration number (alphanumeric)
        if (!registrationField.getText().matches("^[A-Za-z0-9\\-\\s]{3,10}$")) {
            showErrorAlert("Validation Error", "Please enter a valid registration number");
            return false;
        }
        
        // Validate year (reasonable range)
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            int currentYear = LocalDate.now().getYear();
            if (year < 1950 || year > currentYear + 1) {
                showErrorAlert("Validation Error", "Please enter a valid year of manufacture (1950-" + (currentYear + 1) + ")");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Please enter a valid year of manufacture");
            return false;
        }
        
        // Validate insurance expiry date
        if (insuranceExpiryPicker.getValue().isBefore(LocalDate.now())) {
            showErrorAlert("Validation Error", "Insurance expiry date cannot be in the past");
            return false;
        }
        
        return true;
    }
    
    private Rider createRiderFromForm() {
        String id = riderIdField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        RiderStatus status = statusComboBox.getValue();
        LocalDate dob = dobPicker.getValue();
        String licenseNumber = licenseNumberField.getText();
        LocalDate licenseExpiry = licenseExpiryPicker.getValue();
        LocalDateTime now = LocalDateTime.now();
        
        // If it's a new rider, generate a new ID
        if (id == null || id.isEmpty()) {
            id = "R" + System.currentTimeMillis();
        }
        
        return new Rider(
            id,
            firstName,
            lastName,
            phone,
            email,
            status,
            dob,
            licenseNumber,
            licenseExpiry,
            selectedRider == null ? now : selectedRider.createdAt(),
            now
        );
    }
    
    private Vehicle createVehicleFromForm() {
        String registrationNumber = registrationField.getText();
        VehicleType type = vehicleTypeComboBox.getValue();
        String make = makeField.getText();
        String model = modelField.getText();
        int year = Integer.parseInt(yearField.getText());
        String color = colorField.getText();
        VehicleStatus status = vehicleStatusComboBox.getValue();
        LocalDate insuranceExpiry = insuranceExpiryPicker.getValue();
        LocalDateTime now = LocalDateTime.now();
        
        Rider selectedRiderForVehicle = riderComboBox.getValue();
        String riderId = selectedRiderForVehicle != null ? selectedRiderForVehicle.id() : null;
        
        return new Vehicle(
            registrationNumber,
            type,
            make,
            model,
            year,
            color,
            status,
            insuranceExpiry,
            riderId,
            selectedVehicle == null ? now : selectedVehicle.createdAt(),
            now
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