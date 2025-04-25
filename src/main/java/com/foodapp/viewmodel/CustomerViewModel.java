package com.foodapp.viewmodel;

import com.foodapp.dao.CustomerDAO;
import com.foodapp.model.Customer;
import com.foodapp.model.Customer.CustomerStatus;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDateTime;

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
    
    // For search functionality
    private final StringProperty searchQuery = new SimpleStringProperty("");
    
    public CustomerViewModel() {
        this.customerDAO = new CustomerDAO();
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
    }
    
    private Customer createCustomerFromForm() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = isExistingCustomer() ? selectedCustomer.get().createdAt() : now;
        
        return new Customer(
                username.get(),
                email.get(),
                phone.get(),
                firstName.get(),
                lastName.get(),
                password.get(),
                status.get(),
                createdAt,
                now
        );
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
} 