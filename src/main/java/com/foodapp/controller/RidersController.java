package com.foodapp.controller;

import com.foodapp.model.Rider;
import com.foodapp.model.Rider.RiderStatus;
import com.foodapp.model.Vehicle;
import com.foodapp.model.Vehicle.VehicleStatus;
import com.foodapp.model.Vehicle.VehicleType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private TableColumn<Rider, String> riderVehicleColumn;
    
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
    
    @FXML
    private ComboBox<Vehicle> vehicleComboBox;
    
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
    
    private ObservableList<Rider> riders = FXCollections.observableArrayList();
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
    
    private Rider selectedRider;
    private Vehicle selectedVehicle;
    
    @FXML
    private void initialize() {
        // Configure rider table columns
        riderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        riderNameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFullName()));
        
        riderPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        riderEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        riderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        riderVehicleColumn.setCellValueFactory(data -> {
            Rider rider = data.getValue();
            if (rider.hasAssignedVehicle()) {
                return new SimpleStringProperty(rider.assignedVehicle().getDescription());
            } else {
                return new SimpleStringProperty("No vehicle assigned");
            }
        });
        
        // Configure vehicle table columns
        registrationColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        makeModelColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().make() + " " + data.getValue().model()));
        
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearOfManufacture"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        assignedToColumn.setCellValueFactory(data -> {
            Vehicle vehicle = data.getValue();
            if (vehicle.isAssigned()) {
                for (Rider rider : riders) {
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
        
        // Load mock data
        loadMockData();
        
        // Update display
        ridersTableView.setItems(riders);
        vehiclesTableView.setItems(vehicles);
        
        // Setup vehicle and rider combo boxes
        updateRiderComboBox();
        updateVehicleComboBox();
    }
    
    private void loadMockData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create some vehicles
        Vehicle v1 = new Vehicle(
            "HDR-1234",
            VehicleType.MOTORCYCLE,
            "Honda",
            "CBR125",
            2021,
            "Red",
            VehicleStatus.ACTIVE,
            LocalDate.now().plusYears(1),
            "RID-001",
            now.minusDays(100),
            now.minusDays(100)
        );
        
        Vehicle v2 = new Vehicle(
            "TYT-5678",
            VehicleType.CAR,
            "Toyota",
            "Prius",
            2020,
            "Silver",
            VehicleStatus.ACTIVE,
            LocalDate.now().plusMonths(6),
            "RID-002",
            now.minusDays(150),
            now.minusDays(150)
        );
        
        Vehicle v3 = new Vehicle(
            "BMX-9012",
            VehicleType.BICYCLE,
            "Trek",
            "FX3",
            2022,
            "Black",
            VehicleStatus.ACTIVE,
            LocalDate.now().plusYears(2),
            "RID-003",
            now.minusDays(90),
            now.minusDays(90)
        );
        
        Vehicle v4 = new Vehicle(
            "FRD-3456",
            VehicleType.VAN,
            "Ford",
            "Transit",
            2019,
            "White",
            VehicleStatus.MAINTENANCE,
            LocalDate.now().plusYears(1),
            null,
            now.minusDays(200),
            now.minusDays(20)
        );
        
        vehicles.addAll(v1, v2, v3, v4);
        
        // Create some riders
        Rider r1 = new Rider(
            "RID-001",
            "John",
            "Doe",
            "+1-555-123-4567",
            "john.doe@example.com",
            RiderStatus.ACTIVE,
            LocalDate.of(1990, 5, 15),
            "DL-123456",
            LocalDate.now().plusYears(3),
            v1,
            now.minusDays(100),
            now.minusDays(100)
        );
        
        Rider r2 = new Rider(
            "RID-002",
            "Jane",
            "Smith",
            "+1-555-234-5678",
            "jane.smith@example.com",
            RiderStatus.ON_DELIVERY,
            LocalDate.of(1992, 8, 22),
            "DL-234567",
            LocalDate.now().plusYears(4),
            v2,
            now.minusDays(150),
            now.minusDays(150)
        );
        
        Rider r3 = new Rider(
            "RID-003",
            "Michael",
            "Johnson",
            "+1-555-345-6789",
            "michael.johnson@example.com",
            RiderStatus.ACTIVE,
            LocalDate.of(1988, 3, 10),
            "DL-345678",
            LocalDate.now().plusYears(2),
            v3,
            now.minusDays(90),
            now.minusDays(90)
        );
        
        Rider r4 = new Rider(
            "RID-004",
            "Emily",
            "Williams",
            "+1-555-456-7890",
            "emily.williams@example.com",
            RiderStatus.INACTIVE,
            LocalDate.of(1995, 11, 28),
            "DL-456789",
            LocalDate.now().plusYears(5),
            now.minusDays(80),
            now.minusDays(15)
        );
        
        riders.addAll(r1, r2, r3, r4);
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
        
        if (rider.hasAssignedVehicle()) {
            for (Vehicle vehicle : vehicles) {
                if (vehicle.registrationNumber().equals(rider.assignedVehicle().registrationNumber())) {
                    vehicleComboBox.setValue(vehicle);
                    break;
                }
            }
        } else {
            vehicleComboBox.setValue(null);
        }
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
            for (Rider rider : riders) {
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
        vehicleComboBox.setValue(null);
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
        riderComboBox.setItems(riders);
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
    
    private void updateVehicleComboBox() {
        vehicleComboBox.setItems(FXCollections.observableArrayList(
            vehicles.filtered(vehicle -> vehicle.status() == VehicleStatus.ACTIVE && !vehicle.isAssigned())
        ));
        
        vehicleComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                if (empty || vehicle == null) {
                    setText(null);
                } else {
                    setText(vehicle.registrationNumber() + " - " + vehicle.getDescription());
                }
            }
        });
        
        vehicleComboBox.setButtonCell(new javafx.scene.control.ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                if (empty || vehicle == null) {
                    setText(null);
                } else {
                    setText(vehicle.registrationNumber() + " - " + vehicle.getDescription());
                }
            }
        });
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        
        if (searchText.isEmpty()) {
            ridersTableView.setItems(riders);
            vehiclesTableView.setItems(vehicles);
        } else {
            // Filter riders
            ObservableList<Rider> filteredRiders = riders.filtered(rider ->
                rider.id().toLowerCase().contains(searchText) ||
                rider.firstName().toLowerCase().contains(searchText) ||
                rider.lastName().toLowerCase().contains(searchText) ||
                rider.phone().toLowerCase().contains(searchText) ||
                rider.email().toLowerCase().contains(searchText)
            );
            ridersTableView.setItems(filteredRiders);
            
            // Filter vehicles
            ObservableList<Vehicle> filteredVehicles = vehicles.filtered(vehicle ->
                vehicle.registrationNumber().toLowerCase().contains(searchText) ||
                vehicle.make().toLowerCase().contains(searchText) ||
                vehicle.model().toLowerCase().contains(searchText) ||
                vehicle.color().toLowerCase().contains(searchText)
            );
            vehiclesTableView.setItems(filteredVehicles);
        }
    }
    
    @FXML
    private void handleSaveRider() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Rider");
        alert.setHeaderText("Not Implemented");
        alert.setContentText("This feature would save the rider details");
        alert.showAndWait();
    }
    
    @FXML
    private void handleDeleteRider() {
        if (selectedRider != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Delete Rider");
            alert.setHeaderText("Not Implemented");
            alert.setContentText("This feature would delete the rider: " + selectedRider.getFullName());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleSaveVehicle() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Vehicle");
        alert.setHeaderText("Not Implemented");
        alert.setContentText("This feature would save the vehicle details");
        alert.showAndWait();
    }
    
    @FXML
    private void handleDeleteVehicle() {
        if (selectedVehicle != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Delete Vehicle");
            alert.setHeaderText("Not Implemented");
            alert.setContentText("This feature would delete the vehicle: " + selectedVehicle.registrationNumber());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleRefresh() {
        // In a real app, this would reload from the database
        riders.clear();
        vehicles.clear();
        loadMockData();
        ridersTableView.setItems(riders);
        vehiclesTableView.setItems(vehicles);
        updateRiderComboBox();
        updateVehicleComboBox();
    }
} 