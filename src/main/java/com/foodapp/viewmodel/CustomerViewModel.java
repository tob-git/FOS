package com.foodapp.viewmodel;

import com.foodapp.dao.CustomerDAO;
import com.foodapp.model.Address;
import com.foodapp.model.Customer;
import com.foodapp.model.Customer.CustomerStatus;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerViewModel {
    
    private final CustomerDAO customerDAO;
    
    // Observable properties for binding to UI
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>();
    
    // Properties for customer form
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<CustomerStatus> status = new SimpleObjectProperty<>(CustomerStatus.ACTIVE);
    
    // For address form
    private final ObservableList<Address> addressList = FXCollections.observableArrayList();
    private final ObjectProperty<Address> selectedAddress = new SimpleObjectProperty<>();
    private final StringProperty street = new SimpleStringProperty();
    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty state = new SimpleStringProperty();
    private final StringProperty postalCode = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty("United States");
    private final ObjectProperty<BigDecimal> latitude = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> longitude = new SimpleObjectProperty<>();
    
    // For search functionality
    private final StringProperty searchQuery = new SimpleStringProperty("");
    
    public CustomerViewModel() {
        try {
            this.customerDAO = new CustomerDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize CustomerDAO", e);
        }
    }
    
    public CustomerViewModel(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }
    
    public void loadAllCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
            // In a real application, you'd want to handle this more gracefully
            // and inform the user about the error
        }
    }
    
    public void searchCustomers() {
        try {
            if (searchQuery.get() == null || searchQuery.get().isBlank()) {
                loadAllCustomers();
            } else {
                customerList.clear();
                customerList.addAll(customerDAO.search(searchQuery.get()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveCustomer() throws SQLException {
        Customer customer = createCustomerFromForm();
        
        if (isExistingCustomer()) {
            customerDAO.update(customer);
        } else {
            customerDAO.insert(customer);
        }
        
        loadAllCustomers();
        clearForm();
    }
    
    public void deleteCustomer() throws SQLException {
        if (selectedCustomer.get() != null) {
            customerDAO.delete(selectedCustomer.get().username());
            loadAllCustomers();
            clearForm();
        }
    }
    
    public void selectCustomer(Customer customer) {
        selectedCustomer.set(customer);
        
        if (customer != null) {
            // Populate form fields with selected customer's data
            username.set(customer.username());
            email.set(customer.email());
            phone.set(customer.phone());
            firstName.set(customer.firstName());
            lastName.set(customer.lastName());
            password.set(customer.password());
            status.set(customer.status());
            
            // Load addresses
            addressList.clear();
            if (customer.addresses() != null) {
                addressList.addAll(customer.addresses());
            }
        } else {
            clearForm();
        }
    }
    
    public void clearForm() {
        username.set("");
        email.set("");
        phone.set("");
        firstName.set("");
        lastName.set("");
        password.set("");
        status.set(CustomerStatus.ACTIVE);
        selectedCustomer.set(null);
        
        // Clear address form
        clearAddressForm();
    }
    
    public void clearAddressForm() {
        street.set("");
        city.set("");
        state.set("");
        postalCode.set("");
        country.set("United States");
        latitude.set(null);
        longitude.set(null);
        selectedAddress.set(null);
    }
    
    private Customer createCustomerFromForm() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = isExistingCustomer() ? selectedCustomer.get().createdAt() : now;
        
        // Keep existing addresses if updating a customer
        List<Address> addresses = new ArrayList<>();
        if (isExistingCustomer() && selectedCustomer.get().addresses() != null) {
            addresses.addAll(selectedCustomer.get().addresses());
        }
        
        return new Customer(
                username.get(),
                email.get(),
                phone.get(),
                firstName.get(),
                lastName.get(),
                password.get(),
                status.get(),
                addresses,
                createdAt,
                now
        );
    }
    
    public void selectAddress(Address address) {
        selectedAddress.set(address);
        
        if (address != null) {
            street.set(address.street());
            city.set(address.city());
            state.set(address.state());
            postalCode.set(address.postalCode());
            country.set(address.country());
            latitude.set(address.latitude());
            longitude.set(address.longitude());
        } else {
            clearAddressForm();
        }
    }
    
    public void saveAddress() throws SQLException {
        if (selectedCustomer.get() == null) {
            throw new IllegalStateException("Cannot add address without a selected customer");
        }
        
        Address address = createAddressFromForm();
        
        if (selectedAddress.get() != null && !selectedAddress.get().isNew()) {
            // Update existing address
            customerDAO.updateAddress(address);
            
            // Update local list
            int index = addressList.indexOf(selectedAddress.get());
            if (index >= 0) {
                addressList.set(index, address);
            }
        } else {
            // Add new address
            Address savedAddress = customerDAO.addAddress(address);
            addressList.add(address);
            
            // Update the selected customer with the new address
            Customer updatedCustomer = selectedCustomer.get().withAddress(address);
            selectedCustomer.set(updatedCustomer);
        }
        
        clearAddressForm();
    }
    
    public void deleteAddress() throws SQLException {
        if (selectedAddress.get() != null && !selectedAddress.get().isNew()) {
            customerDAO.deleteAddress(selectedAddress.get().id());
            addressList.remove(selectedAddress.get());
            clearAddressForm();
        }
    }
    
    private Address createAddressFromForm() {
        // If updating an existing address
        if (selectedAddress.get() != null && !selectedAddress.get().isNew()) {
            return new Address(
                selectedAddress.get().id(),
                street.get(),
                city.get(),
                state.get(),
                postalCode.get(),
                country.get(),
                latitude.get(),
                longitude.get(),
                selectedCustomer.get().username()
            );
        } else {
            // New address
            return new Address(
                street.get(),
                city.get(),
                state.get(),
                postalCode.get(),
                country.get(),
                latitude.get(),
                longitude.get(),
                selectedCustomer.get().username()
            );
        }
    }
    
    private boolean isExistingCustomer() {
        return selectedCustomer.get() != null;
    }
    
    // Getters for collections and properties
    
    public ObservableList<Customer> getCustomerList() {
        return customerList;
    }
    
    public ObjectProperty<Customer> selectedCustomerProperty() {
        return selectedCustomer;
    }
    
    public StringProperty usernameProperty() {
        return username;
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public StringProperty phoneProperty() {
        return phone;
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    
    public StringProperty passwordProperty() {
        return password;
    }
    
    public ObjectProperty<CustomerStatus> statusProperty() {
        return status;
    }
    
    public StringProperty searchQueryProperty() {
        return searchQuery;
    }
    
    // Address properties
    
    public ObservableList<Address> getAddressList() {
        return addressList;
    }
    
    public ObjectProperty<Address> selectedAddressProperty() {
        return selectedAddress;
    }
    
    public StringProperty streetProperty() {
        return street;
    }
    
    public StringProperty cityProperty() {
        return city;
    }
    
    public StringProperty stateProperty() {
        return state;
    }
    
    public StringProperty postalCodeProperty() {
        return postalCode;
    }
    
    public StringProperty countryProperty() {
        return country;
    }
    
    public ObjectProperty<BigDecimal> latitudeProperty() {
        return latitude;
    }
    
    public ObjectProperty<BigDecimal> longitudeProperty() {
        return longitude;
    }
} 