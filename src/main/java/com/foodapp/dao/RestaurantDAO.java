package com.foodapp.dao;

import com.foodapp.model.Address;
import com.foodapp.model.Restaurant;
import com.foodapp.model.Restaurant.RestaurantStatus;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {

    private static final String SELECT_ALL_RESTAURANTS = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id ORDER BY r.name";
    private static final String SELECT_RESTAURANT_BY_SLUG = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id WHERE r.slug = ?";
    private static final String INSERT_ADDRESS = "INSERT INTO addresses (street, city, state, postal_code, country, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_RESTAURANT = "INSERT INTO restaurants (slug, name, description, logo_url, phone, email, website, status, opening_time, closing_time, address_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ADDRESS = "UPDATE addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ?, latitude = ?, longitude = ? WHERE id = ?";
    private static final String UPDATE_RESTAURANT = "UPDATE restaurants SET name = ?, description = ?, logo_url = ?, phone = ?, email = ?, website = ?, status = ?, opening_time = ?, closing_time = ?, updated_at = ? WHERE slug = ?";
    private static final String DELETE_RESTAURANT = "DELETE FROM restaurants WHERE slug = ?";
    private static final String SEARCH_RESTAURANTS = "SELECT r.*, a.* FROM restaurants r JOIN addresses a ON r.address_id = a.id WHERE r.name LIKE ? OR r.description LIKE ? OR a.city LIKE ? ORDER BY r.name";

    private final Connection connection;

    public RestaurantDAO() throws SQLException {
        this.connection = DatabaseManager.getConnection();
    }

    public List<Restaurant> findAll() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT \n" +
                "    r.slug,\n" +
                "    r.name,\n" +
                "    r.description,\n" +
                "    r.logo_url,\n" +
                "    r.phone,\n" +
                "    r.email,\n" +
                "    r.website,\n" +
                "    r.status,\n" +
                "    r.opening_time,\n" +
                "    r.closing_time,\n" +
                "    a.id AS address_id,\n" +
                "    a.street,\n" +
                "    a.city,\n" +
                "    a.state,\n" +
                "    a.postal_code,\n" +
                "    a.country,\n" +
                "    a.latitude,\n" +
                "    a.longitude,\n" +
                "a.customer_username,\n"+
                "a.created_at,\n"+
                "a.updated_at\n"+
                "FROM " +
                "    restaurants r\n" +
                "JOIN \n" +
                "    addresses a ON r.address_id = a.id\n";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.print('4');
                restaurants.add(mapToRestaurant(rs));
                System.out.print('5');
            }
        }
        
        return restaurants;
    }
    
    public Restaurant findBySlug(String slug) throws SQLException {
        Restaurant restaurant = null;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM restaurants WHERE slug = '" + slug + "'")) {
            if (rs.next()) {
                System.out.print('6');
                restaurant = mapToRestaurant(rs);
                System.out.print('7');
            }
        }
        
        return restaurant;
    }
    
    public void insert(Restaurant restaurant) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_RESTAURANT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, restaurant.slug());
            stmt.setString(2, restaurant.name());
            stmt.setString(3, restaurant.description());
            stmt.setString(4, restaurant.logoUrl());
            stmt.setString(5, restaurant.phone());
            stmt.setString(6, restaurant.email());
            stmt.setString(7, restaurant.website());
            stmt.setString(8, restaurant.status().toString());
            stmt.setObject(9, restaurant.openingTime());
            stmt.setObject(10, restaurant.closingTime());
            stmt.setInt(11, restaurant.address().id());
            stmt.setObject(12, restaurant.createdAt());
            stmt.setObject(13, restaurant.updatedAt());
            
            stmt.executeUpdate();
            
            // Since Address is a record and doesn't have setId method, we can't set the ID
            // We would need to create a new Address instance with the updated ID
            // try (ResultSet rs = stmt.getGeneratedKeys()) {
            //     if (rs.next()) {
            //         restaurant.address().setId(rs.getInt(1));
            //     }
            // }
        }
    }
    
    public void update(Restaurant restaurant) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_RESTAURANT)) {
            stmt.setString(1, restaurant.name());
            stmt.setString(2, restaurant.description());
            stmt.setString(3, restaurant.logoUrl());
            stmt.setString(4, restaurant.phone());
            stmt.setString(5, restaurant.email());
            stmt.setString(6, restaurant.website());
            stmt.setString(7, restaurant.status().toString());
            stmt.setObject(8, restaurant.openingTime());
            stmt.setObject(9, restaurant.closingTime());
            stmt.setObject(10, restaurant.updatedAt());
            stmt.setString(11, restaurant.slug());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String slug) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_RESTAURANT)) {
            stmt.setString(1, slug);
            stmt.executeUpdate();
        }
    }
    
    public List<Restaurant> search(String searchText) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT * FROM restaurants WHERE name LIKE ? OR description LIKE ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.print('8');
                    restaurants.add(mapToRestaurant(rs));
                    System.out.print('9');
                }
            }
        }
        
        return restaurants;
    }
    
    public List<Menu> findMenusByRestaurant(String restaurantSlug) throws SQLException {
        List<Menu> menus = new ArrayList<>();
        String query = "SELECT * FROM menus WHERE restaurant_slug = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, restaurantSlug);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(10);
                    menus.add(mapToMenu(rs));
                    System.out.println(11);

                }
            }
        }
        
        return menus;
    }

    public List<MenuItem> findMenuItemsByMenu(int menuId) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE menu_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, menuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(12);
                    items.add(mapToMenuItem(rs));
                    System.out.println(13);
                }
            }
        }
        
        return items;
    }

    public void insertMenuItem(MenuItem menuItem) throws SQLException {
        String query = "INSERT INTO menu_items (name, description, price, image_url, category, is_available, menu_id, created_at, updated_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, menuItem.name());
            stmt.setString(2, menuItem.description());
            stmt.setBigDecimal(3, menuItem.price());
            stmt.setString(4, menuItem.imageUrl());
            stmt.setString(5, menuItem.category().toString());
            stmt.setBoolean(6, menuItem.isAvailable());
            stmt.setInt(7, menuItem.menuId());
            stmt.setObject(8, menuItem.createdAt());
            stmt.setObject(9, menuItem.updatedAt());
            
            stmt.executeUpdate();
            
            // Since MenuItem is a record and doesn't have setId method, we can't set the ID
            // We would need to create a new MenuItem instance with the updated ID
            // try (ResultSet rs = stmt.getGeneratedKeys()) {
            //     if (rs.next()) {
            //         menuItem.setId(rs.getInt(1));
            //     }
            // }
        }
    }

    public void updateMenuItem(MenuItem menuItem) throws SQLException {
        String query = "UPDATE menu_items SET name = ?, description = ?, price = ?, image_url = ?, " +
                      "category = ?, is_available = ?, menu_id = ?, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, menuItem.name());
            stmt.setString(2, menuItem.description());
            stmt.setBigDecimal(3, menuItem.price());
            stmt.setString(4, menuItem.imageUrl());
            stmt.setString(5, menuItem.category().toString());
            stmt.setBoolean(6, menuItem.isAvailable());
            stmt.setInt(7, menuItem.menuId());
            stmt.setObject(8, menuItem.updatedAt());
            stmt.setInt(9, menuItem.id());
            
            stmt.executeUpdate();
        }
    }

    public void deleteMenuItem(int menuItemId) throws SQLException {
        String query = "DELETE FROM menu_items WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, menuItemId);
            stmt.executeUpdate();
        }
    }

    public List<String> findAllSlugs() throws SQLException {
        List<String> slugs = new ArrayList<>();
        String query = "SELECT slug FROM restaurants";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                slugs.add(rs.getString("slug"));
            }
        }
        
        return slugs;
    }

    private Restaurant mapToRestaurant(ResultSet rs) throws SQLException {
        return new Restaurant(
            rs.getString("slug"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("logo_url"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("website"),
            RestaurantStatus.valueOf(rs.getString("status")),
            rs.getTime("opening_time").toLocalTime(),
            rs.getTime("closing_time").toLocalTime(),
            new Address(
                rs.getInt("address_id"),
                rs.getString("street"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("postal_code"),
                rs.getString("country"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("customer_username")
            ),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );

    }

    private Menu mapToMenu(ResultSet rs) throws SQLException {
        return new Menu(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            Menu.MenuStatus.valueOf(rs.getString("status")),
            rs.getString("restaurant_slug"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    private MenuItem mapToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("image_url"),
            MenuItem.MenuItemCategory.valueOf(rs.getString("category")),
            rs.getBoolean("is_available"),
            rs.getInt("menu_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
} 