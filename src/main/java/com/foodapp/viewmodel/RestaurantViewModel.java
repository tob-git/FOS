package com.foodapp.viewmodel;

import com.foodapp.dao.RestaurantDAO;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class RestaurantViewModel {
    private final RestaurantDAO restaurantDAO;
    private final ObservableList<Restaurant> restaurants;
    private final ObservableList<Menu> menus;
    private final ObservableList<MenuItem> menuItems;

    public RestaurantViewModel() {
        this.restaurants = FXCollections.observableArrayList();
        this.menus = FXCollections.observableArrayList();
        this.menuItems = FXCollections.observableArrayList();
        System.out.print(13);
        try {
            this.restaurantDAO = new RestaurantDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize RestaurantDAO", e);
        }
    }

    public ObservableList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public ObservableList<Menu> getMenus() {
        return menus;
    }

    public ObservableList<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void loadRestaurants() throws SQLException {
        List<Restaurant> restaurantsList = restaurantDAO.findAll();
        restaurants.clear();
        restaurants.addAll(restaurantsList);
    }

    public void searchRestaurants(String searchText) throws SQLException {
        List<Restaurant> searchResults = restaurantDAO.search(searchText);
        restaurants.clear();
        restaurants.addAll(searchResults);
    }

    public void loadMenus(String restaurantSlug) throws SQLException {
        List<Menu> menusList = restaurantDAO.findMenusByRestaurant(restaurantSlug);
        menus.clear();
        menus.addAll(menusList);
    }

    public void loadMenuItems(int menuId) throws SQLException {
        List<MenuItem> itemsList = restaurantDAO.findMenuItemsByMenu(menuId);
        menuItems.clear();
        menuItems.addAll(itemsList);
    }

    public void addMenuItem(MenuItem menuItem) throws SQLException {
        restaurantDAO.insertMenuItem(menuItem);
        loadMenuItems(menuItem.menuId());
    }

    public void updateMenuItem(MenuItem menuItem) throws SQLException {
        restaurantDAO.updateMenuItem(menuItem);
        loadMenuItems(menuItem.menuId());
    }

    public void deleteMenuItem(int menuItemId, int menuId) throws SQLException {
        restaurantDAO.deleteMenuItem(menuItemId);
        loadMenuItems(menuId);
    }
} 