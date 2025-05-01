package com.foodapp.controller;

import com.foodapp.model.Address;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuItem;
import com.foodapp.model.MenuItem.MenuItemCategory;
import com.foodapp.model.Restaurant;
import com.foodapp.model.Restaurant.RestaurantStatus;
import com.foodapp.viewmodel.RestaurantViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Restaurants view
 */
public class RestaurantsController {

    @FXML
    private ListView<String> restaurantListView;
    
    @FXML
    private TreeTableView<MenuRow> menuTreeTableView;
    
    @FXML
    private TreeTableColumn<MenuRow, String> itemNameColumn;
    
    @FXML
    private TreeTableColumn<MenuRow, String> itemDescriptionColumn;
    
    @FXML
    private TreeTableColumn<MenuRow, String> itemPriceColumn;
    
    @FXML
    private TreeTableColumn<MenuRow, String> itemCategoryColumn;
    
    @FXML
    private TreeTableColumn<MenuRow, Boolean> itemAvailableColumn;
    
    @FXML
    private Label restaurantNameLabel;
    
    @FXML
    private Label restaurantStatusLabel;
    
    @FXML
    private Label restaurantAddressLabel;
    
    @FXML
    private Label restaurantHoursLabel;
    
    @FXML
    private TextArea restaurantDescriptionArea;
    
    @FXML
    private TextField restaurantSearchField;
    
    @FXML
    private Button addMenuItemButton;
    
    @FXML
    private Button editMenuItemButton;
    
    @FXML
    private Button deleteMenuItemButton;
    
    // Form fields for menu item editing
    @FXML
    private TextField itemNameField;
    
    @FXML
    private TextArea itemDescriptionArea;
    
    @FXML
    private TextField itemPriceField;
    
    @FXML
    private ComboBox<String> itemCategoryComboBox;
    
    @FXML
    private CheckBox itemAvailableCheckbox;
    
    @FXML
    private ComboBox<String> menuSelectionComboBox;
    
    private final RestaurantViewModel restaurantViewModel;
    private Map<String, List<Menu>> restaurantMenus = new HashMap<>();
    private Map<Integer, List<MenuItem>> menuItems = new HashMap<>();
    
    private Restaurant selectedRestaurant;
    private MenuRow selectedMenuRow;
    
    public RestaurantsController() {
        System.out.print(15);
        this.restaurantViewModel = new RestaurantViewModel();
    }
    
    /**
     * Helper class to represent rows in the menu tree table
     */
    public static class MenuRow {
        private final String name;
        private final String description;
        private final String price;
        private final String category;
        private final Boolean available;
        private final Object item; // Can be Menu or MenuItem
        
        public MenuRow(Menu menu) {
            this.name = menu.name();
            this.description = menu.description();
            this.price = "";
            this.category = "";
            this.available = true;
            this.item = menu;
        }
        
        public MenuRow(MenuItem menuItem) {
            this.name = menuItem.name();
            this.description = menuItem.description();
            this.price = menuItem.getFormattedPrice();
            this.category = menuItem.category().toString();
            this.available = menuItem.isAvailable();
            this.item = menuItem;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getPrice() { return price; }
        public String getCategory() { return category; }
        public Boolean getAvailable() { return available; }
        public Object getItem() { return item; }
        
        public boolean isMenu() { return item instanceof Menu; }
        public boolean isMenuItem() { return item instanceof MenuItem; }
        
        public Menu asMenu() { return (Menu) item; }
        public MenuItem asMenuItem() { return (MenuItem) item; }
    }
    
    @FXML
    private void initialize() {
        // Initialize tables and list view
        setupTreeTableColumns();
        
        // Set up selection listener for restaurant list
        restaurantListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                for (Restaurant restaurant : restaurantViewModel.getRestaurants()) {
                    if (restaurant.name().equals(newValue)) {
                        displayRestaurantDetails(restaurant);
                        break;
                    }
                }
            }
        });
        
        // Setup control disable states
        addMenuItemButton.setDisable(true);
        editMenuItemButton.setDisable(true);
        deleteMenuItemButton.setDisable(true);
        
        // Setup selection listener for menu tree table
        menuTreeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMenuRow = newSelection.getValue();
                if (selectedMenuRow.isMenuItem()) {
                    editMenuItemButton.setDisable(false);
                    deleteMenuItemButton.setDisable(false);
                    populateMenuItemForm(selectedMenuRow.asMenuItem());
                } else {
                    editMenuItemButton.setDisable(true);
                    deleteMenuItemButton.setDisable(true);
                    clearMenuItemForm();
                }
            } else {
                selectedMenuRow = null;
                editMenuItemButton.setDisable(true);
                deleteMenuItemButton.setDisable(true);
                clearMenuItemForm();
            }
        });
        
        // Load data from database
        loadRestaurants();
        displayRestaurantList();
    }
    
    private void setupTreeTableColumns() {
        itemNameColumn.setCellValueFactory(param -> {
            if (param.getValue().getValue() != null) {
                return new ReadOnlyStringWrapper(param.getValue().getValue().getName());
            }
            return new ReadOnlyStringWrapper("");
        });
        
        itemDescriptionColumn.setCellValueFactory(param -> {
            if (param.getValue().getValue() != null) {
                return new ReadOnlyStringWrapper(param.getValue().getValue().getDescription());
            }
            return new ReadOnlyStringWrapper("");
        });
        
        itemPriceColumn.setCellValueFactory(param -> {
            if (param.getValue().getValue() != null) {
                return new ReadOnlyStringWrapper(param.getValue().getValue().getPrice());
            }
            return new ReadOnlyStringWrapper("");
        });
        
        itemCategoryColumn.setCellValueFactory(param -> {
            if (param.getValue().getValue() != null) {
                return new ReadOnlyStringWrapper(param.getValue().getValue().getCategory());
            }
            return new ReadOnlyStringWrapper("");
        });
        
        itemAvailableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("available"));
    }
    
    private void loadRestaurants() {
        try {
            restaurantViewModel.loadRestaurants();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load restaurants: " + e.getMessage());
        }
    }
    
    private void displayRestaurantList() {
        ObservableList<String> restaurantNames = FXCollections.observableArrayList();
        for (Restaurant restaurant : restaurantViewModel.getRestaurants()) {
            restaurantNames.add(restaurant.name());
        }
        restaurantListView.setItems(restaurantNames);
        
        // Select the first restaurant by default
        if (!restaurantNames.isEmpty()) {
            restaurantListView.getSelectionModel().select(0);
        }
    }
    
    private void displayRestaurantDetails(Restaurant restaurant) {
        selectedRestaurant = restaurant;
        
        // Update the restaurant details display
        restaurantNameLabel.setText(restaurant.name());
        restaurantStatusLabel.setText("Status: " + restaurant.status());
        
        // Format and display the address
        Address address = restaurant.address();
        System.out.print('1');
        String formattedAddress = address.street();
        System.out.print('2');
        formattedAddress += ", " + address.city() + ", " + address.state() + " " + address.postalCode();
        restaurantAddressLabel.setText(formattedAddress);
        
        // Format and display hours
        String hours = "Hours: " + restaurant.openingTime().toString() + " - " + restaurant.closingTime().toString();
        restaurantHoursLabel.setText(hours);
        
        restaurantDescriptionArea.setText(restaurant.description());
        
        // Enable the add menu item button
        addMenuItemButton.setDisable(false);
        
        // Display the menu tree
        displayMenuTree(restaurant.slug());
    }
    
    private void displayMenuTree(String restaurantSlug) {
        try {
            restaurantViewModel.loadMenus(restaurantSlug);
            
            // Clear the current tree
            menuTreeTableView.setRoot(null);
            
            // Create the root item
            TreeItem<MenuRow> rootItem = new TreeItem<>(new MenuRow(new Menu(0, "All Menus", "", Menu.MenuStatus.ACTIVE, restaurantSlug, LocalDateTime.now(), LocalDateTime.now())));
            rootItem.setExpanded(true);
            
            // Get menus for this restaurant
            for (Menu menu : restaurantViewModel.getMenus()) {
                TreeItem<MenuRow> menuItem = new TreeItem<>(new MenuRow(menu));
                rootItem.getChildren().add(menuItem);
                
                // Add menu items under this menu
                restaurantViewModel.loadMenuItems(menu.id());
                for (MenuItem item : restaurantViewModel.getMenuItems()) {
                    TreeItem<MenuRow> menuItemNode = new TreeItem<>(new MenuRow(item));
                    menuItem.getChildren().add(menuItemNode);
                }
                menuItem.setExpanded(true);
            }
            
            menuTreeTableView.setRoot(rootItem);
            menuTreeTableView.setShowRoot(false);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load menus: " + e.getMessage());
        }
    }
    
    private void populateMenuItemForm(MenuItem menuItem) {
        itemNameField.setText(menuItem.name());
        itemDescriptionArea.setText(menuItem.description());
        itemPriceField.setText(menuItem.price().toString());
        itemCategoryComboBox.setValue(menuItem.category().toString());
        itemAvailableCheckbox.setSelected(menuItem.isAvailable());
        
        // Set the menu combobox (if available)
        if (menuSelectionComboBox != null) {
            for (Menu menu : restaurantMenus.get(selectedRestaurant.slug())) {
                if (menu.id() == menuItem.menuId()) {
                    menuSelectionComboBox.setValue(menu.name());
                    break;
                }
            }
        }
    }
    
    private void clearMenuItemForm() {
        itemNameField.clear();
        itemDescriptionArea.clear();
        itemPriceField.clear();
        itemCategoryComboBox.setValue(null);
        itemAvailableCheckbox.setSelected(false);
        
        if (menuSelectionComboBox != null) {
            menuSelectionComboBox.setValue(null);
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadRestaurants();
        displayRestaurantList();
    }
    
    @FXML
    private void handleRestaurantSearch() {
        String searchText = restaurantSearchField.getText().toLowerCase();
        
        try {
            restaurantViewModel.searchRestaurants(searchText);
            ObservableList<String> filteredRestaurants = FXCollections.observableArrayList();
            for (Restaurant restaurant : restaurantViewModel.getRestaurants()) {
                filteredRestaurants.add(restaurant.name());
            }
            restaurantListView.setItems(filteredRestaurants);
        } catch (SQLException e) {
            showErrorAlert("Search Error", "Failed to search restaurants: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddMenuItem() {
        if (selectedRestaurant != null) {
            try {
                MenuItem newMenuItem = createMenuItemFromForm();
                restaurantViewModel.addMenuItem(newMenuItem);
                displayMenuTree(selectedRestaurant.slug());
                clearMenuItemForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to add menu item: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEditMenuItem() {
        if (selectedMenuRow != null && selectedMenuRow.isMenuItem()) {
            try {
                MenuItem updatedMenuItem = createMenuItemFromForm();
                restaurantViewModel.updateMenuItem(updatedMenuItem);
                displayMenuTree(selectedRestaurant.slug());
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update menu item: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDeleteMenuItem() {
        if (selectedMenuRow != null && selectedMenuRow.isMenuItem()) {
            try {
                MenuItem menuItem = selectedMenuRow.asMenuItem();
                restaurantViewModel.deleteMenuItem(menuItem.id(), menuItem.menuId());
                displayMenuTree(selectedRestaurant.slug());
                clearMenuItemForm();
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete menu item: " + e.getMessage());
            }
        }
    }
    
    private MenuItem createMenuItemFromForm() {
        String name = itemNameField.getText().trim();
        String description = itemDescriptionArea.getText().trim();
        BigDecimal price = new BigDecimal(itemPriceField.getText().trim());
        MenuItemCategory category = MenuItemCategory.valueOf(itemCategoryComboBox.getValue());
        boolean available = itemAvailableCheckbox.isSelected();
        
        // Get the selected menu
        int menuId = 0; // Default value
        if (menuSelectionComboBox != null && menuSelectionComboBox.getValue() != null) {
            for (Menu menu : restaurantMenus.get(selectedRestaurant.slug())) {
                if (menu.name().equals(menuSelectionComboBox.getValue())) {
                    menuId = menu.id();
                    break;
                }
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        return new MenuItem(
            0, // ID will be set by the database
            name,
            description,
            price,
            "", // imagePath
            category,
            available,
            menuId,
            now,
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