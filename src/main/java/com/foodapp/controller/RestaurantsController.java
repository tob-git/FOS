package com.foodapp.controller;

import com.foodapp.model.Address;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuItem;
import com.foodapp.model.MenuItem.MenuItemCategory;
import com.foodapp.model.Restaurant;
import com.foodapp.model.Restaurant.RestaurantStatus;
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
    
    private ObservableList<Restaurant> restaurants = FXCollections.observableArrayList();
    private Map<String, List<Menu>> restaurantMenus = new HashMap<>();
    private Map<Integer, List<MenuItem>> menuItems = new HashMap<>();
    
    private Restaurant selectedRestaurant;
    private MenuRow selectedMenuRow;
    
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
                for (Restaurant restaurant : restaurants) {
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
        
        // Initialize with mock data
        loadMockData();
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
    
    private void loadMockData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create restaurants
        Restaurant nycPizza = new Restaurant(
            "nyc-pizza",
            "NYC Pizza",
            "Authentic New York style pizza with a wide range of toppings.",
            "nyc_pizza_logo.png",
            "+1-555-123-4567",
            "info@nycpizza.com",
            "http://nycpizza.com",
            RestaurantStatus.OPEN,
            LocalTime.of(10, 0),
            LocalTime.of(22, 0),
            new Address(1, "123 Broadway", "New York", "NY", "10001", "USA", 40.7128, -74.0060, null),
            now.minusDays(100),
            now.minusDays(5)
        );
        
        Restaurant burgerKing = new Restaurant(
            "burger-king",
            "Burger King",
            "Home of the Whopper and other flame-grilled favorites.",
            "burger_king_logo.png",
            "+1-555-234-5678",
            "info@burgerking.com",
            "http://burgerking.com",
            RestaurantStatus.OPEN,
            LocalTime.of(8, 0),
            LocalTime.of(23, 0),
            new Address(2, "456 Main St", "New York", "NY", "10002", "USA", 40.7200, -74.0100, null),
            now.minusDays(200),
            now.minusDays(10)
        );
        
        Restaurant tacobell = new Restaurant(
            "taco-bell",
            "Taco Bell",
            "Mexican-inspired quick service restaurant serving tacos, burritos and more.",
            "taco_bell_logo.png",
            "+1-555-345-6789",
            "info@tacobell.com",
            "http://tacobell.com",
            RestaurantStatus.OPEN,
            LocalTime.of(9, 0),
            LocalTime.of(23, 0),
            new Address(3, "789 Taco St", "New York", "NY", "10003", "USA", 40.7300, -74.0200, null),
            now.minusDays(150),
            now.minusDays(15)
        );
        
        Restaurant sushiExpress = new Restaurant(
            "sushi-express",
            "Sushi Express",
            "Fresh and delicious sushi delivered quickly.",
            "sushi_express_logo.png",
            "+1-555-456-7890",
            "info@sushiexpress.com",
            "http://sushiexpress.com",
            RestaurantStatus.TEMPORARILY_UNAVAILABLE,
            LocalTime.of(11, 0),
            LocalTime.of(22, 30),
            new Address(4, "101 Sushi Ave", "New York", "NY", "10004", "USA", 40.7400, -74.0300, null),
            now.minusDays(80),
            now.minusDays(2)
        );
        
        restaurants.addAll(nycPizza, burgerKing, tacobell, sushiExpress);
        
        // Create menus and menu items
        // NYC Pizza menus
        Menu pizzaMenu = new Menu(1, "Pizza Menu", "Our signature pizzas", Menu.MenuStatus.ACTIVE, "nyc-pizza", now.minusDays(90), now.minusDays(5));
        Menu sidesMenu = new Menu(2, "Sides & Extras", "Complete your meal", Menu.MenuStatus.ACTIVE, "nyc-pizza", now.minusDays(90), now.minusDays(5));
        Menu drinksMenu = new Menu(3, "Drinks", "Beverages to accompany your meal", Menu.MenuStatus.ACTIVE, "nyc-pizza", now.minusDays(90), now.minusDays(5));
        
        List<Menu> nycPizzaMenus = new ArrayList<>();
        nycPizzaMenus.add(pizzaMenu);
        nycPizzaMenus.add(sidesMenu);
        nycPizzaMenus.add(drinksMenu);
        restaurantMenus.put("nyc-pizza", nycPizzaMenus);
        
        // Burger King menus
        Menu burgersMenu = new Menu(4, "Burgers", "Flame-grilled burgers", Menu.MenuStatus.ACTIVE, "burger-king", now.minusDays(180), now.minusDays(10));
        Menu sidesMenuBK = new Menu(5, "Sides", "Fries and more", Menu.MenuStatus.ACTIVE, "burger-king", now.minusDays(180), now.minusDays(10));
        Menu drinksMenuBK = new Menu(6, "Beverages", "Cool drinks", Menu.MenuStatus.ACTIVE, "burger-king", now.minusDays(180), now.minusDays(10));
        
        List<Menu> burgerKingMenus = new ArrayList<>();
        burgerKingMenus.add(burgersMenu);
        burgerKingMenus.add(sidesMenuBK);
        burgerKingMenus.add(drinksMenuBK);
        restaurantMenus.put("burger-king", burgerKingMenus);
        
        // Add some menu items for NYC Pizza
        List<MenuItem> pizzaItems = new ArrayList<>();
        pizzaItems.add(new MenuItem(1, "Margherita", "Classic tomato sauce and mozzarella cheese", new BigDecimal("12.99"), "margherita.jpg", MenuItemCategory.MAIN_COURSE, true, 1, now.minusDays(90), now.minusDays(5)));
        pizzaItems.add(new MenuItem(2, "Pepperoni", "Margherita with pepperoni toppings", new BigDecimal("14.99"), "pepperoni.jpg", MenuItemCategory.MAIN_COURSE, true, 1, now.minusDays(90), now.minusDays(5)));
        pizzaItems.add(new MenuItem(3, "Vegetarian", "Mixed vegetables on a cheese base", new BigDecimal("13.99"), "vegetarian.jpg", MenuItemCategory.MAIN_COURSE, true, 1, now.minusDays(90), now.minusDays(5)));
        menuItems.put(1, pizzaItems);
        
        List<MenuItem> sidesItems = new ArrayList<>();
        sidesItems.add(new MenuItem(4, "Garlic Bread", "Toasted bread with garlic butter", new BigDecimal("4.99"), "garlic_bread.jpg", MenuItemCategory.SIDE, true, 2, now.minusDays(90), now.minusDays(5)));
        sidesItems.add(new MenuItem(5, "Mozzarella Sticks", "Breaded and fried mozzarella", new BigDecimal("6.99"), "mozzarella_sticks.jpg", MenuItemCategory.APPETIZER, true, 2, now.minusDays(90), now.minusDays(5)));
        menuItems.put(2, sidesItems);
        
        List<MenuItem> drinksItems = new ArrayList<>();
        drinksItems.add(new MenuItem(6, "Coke", "Classic Cola", new BigDecimal("1.99"), "coke.jpg", MenuItemCategory.BEVERAGE, true, 3, now.minusDays(90), now.minusDays(5)));
        drinksItems.add(new MenuItem(7, "Sprite", "Lemon-lime soda", new BigDecimal("1.99"), "sprite.jpg", MenuItemCategory.BEVERAGE, true, 3, now.minusDays(90), now.minusDays(5)));
        menuItems.put(3, drinksItems);
    }
    
    private void displayRestaurantList() {
        ObservableList<String> restaurantNames = FXCollections.observableArrayList();
        for (Restaurant restaurant : restaurants) {
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
        String formattedAddress = address.street();
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
        // Clear the current tree
        menuTreeTableView.setRoot(null);
        
        // Create the root item
        TreeItem<MenuRow> rootItem = new TreeItem<>(new MenuRow(new Menu(0, "All Menus", "", Menu.MenuStatus.ACTIVE, restaurantSlug, LocalDateTime.now(), LocalDateTime.now())));
        rootItem.setExpanded(true);
        
        // Get menus for this restaurant
        List<Menu> menus = restaurantMenus.get(restaurantSlug);
        if (menus != null) {
            for (Menu menu : menus) {
                TreeItem<MenuRow> menuItem = new TreeItem<>(new MenuRow(menu));
                rootItem.getChildren().add(menuItem);
                
                // Add menu items under this menu
                List<MenuItem> items = menuItems.get(menu.id());
                if (items != null) {
                    for (MenuItem item : items) {
                        TreeItem<MenuRow> menuItemNode = new TreeItem<>(new MenuRow(item));
                        menuItem.getChildren().add(menuItemNode);
                    }
                    menuItem.setExpanded(true);
                }
            }
        }
        
        menuTreeTableView.setRoot(rootItem);
        menuTreeTableView.setShowRoot(false);
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
    private void handleRestaurantSearch() {
        String searchText = restaurantSearchField.getText().toLowerCase();
        
        ObservableList<String> filteredRestaurants = FXCollections.observableArrayList();
        for (Restaurant restaurant : restaurants) {
            if (restaurant.name().toLowerCase().contains(searchText) || 
                restaurant.description().toLowerCase().contains(searchText)) {
                filteredRestaurants.add(restaurant.name());
            }
        }
        
        restaurantListView.setItems(filteredRestaurants);
    }
    
    @FXML
    private void handleAddMenuItem() {
        // In a real app, this would show a dialog or form to add a new menu item
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Menu Item");
        alert.setHeaderText("Not Implemented");
        alert.setContentText("This feature would allow adding a new menu item to " + selectedRestaurant.name());
        alert.showAndWait();
    }
    
    @FXML
    private void handleEditMenuItem() {
        if (selectedMenuRow != null && selectedMenuRow.isMenuItem()) {
            // In a real app, this would update the database or at least our mock data
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Edit Menu Item");
            alert.setHeaderText("Not Implemented");
            alert.setContentText("This feature would update the menu item: " + selectedMenuRow.getName());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleDeleteMenuItem() {
        if (selectedMenuRow != null && selectedMenuRow.isMenuItem()) {
            // In a real app, this would delete from the database or at least our mock data
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Delete Menu Item");
            alert.setHeaderText("Not Implemented");
            alert.setContentText("This feature would delete the menu item: " + selectedMenuRow.getName());
            alert.showAndWait();
        }
    }
} 