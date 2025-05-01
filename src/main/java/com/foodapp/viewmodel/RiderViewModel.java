package com.foodapp.viewmodel;

import com.foodapp.dao.RiderDAO;
import com.foodapp.model.Rider;
import com.foodapp.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class RiderViewModel {
    private final RiderDAO riderDAO;
    private final ObservableList<Rider> riders;
    private final ObservableList<Vehicle> vehicles;

    public RiderViewModel() {
        this.riders = FXCollections.observableArrayList();
        this.vehicles = FXCollections.observableArrayList();
        
        try {
            this.riderDAO = new RiderDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize RiderDAO", e);
        }
    }

    public ObservableList<Rider> getRiders() {
        return riders;
    }

    public ObservableList<Vehicle> getVehicles() {
        return vehicles;
    }

    public void loadRiders() throws SQLException {
        List<Rider> ridersList = riderDAO.findAll();
        riders.clear();
        riders.addAll(ridersList);
    }

    public void loadVehicles() throws SQLException {
        List<Vehicle> vehiclesList = riderDAO.findAllVehicles();
        vehicles.clear();
        vehicles.addAll(vehiclesList);
    }

    public void searchRiders(String searchText) throws SQLException {
        List<Rider> searchResults = riderDAO.search(searchText);
        riders.clear();
        riders.addAll(searchResults);
    }

    public void addRider(Rider rider) throws SQLException {
        riderDAO.insert(rider);
        loadRiders();
    }

    public void updateRider(Rider rider) throws SQLException {
        riderDAO.update(rider);
        loadRiders();
    }

    public void deleteRider(String riderId) throws SQLException {
        riderDAO.delete(riderId);
        loadRiders();
    }

    public void addVehicle(Vehicle vehicle) throws SQLException {
        riderDAO.insertVehicle(vehicle);
        loadVehicles();
    }

    public void updateVehicle(Vehicle vehicle) throws SQLException {
        riderDAO.updateVehicle(vehicle);
        loadVehicles();
    }

    public void deleteVehicle(String registrationNumber) throws SQLException {
        riderDAO.deleteVehicle(registrationNumber);
        loadVehicles();
    }
} 