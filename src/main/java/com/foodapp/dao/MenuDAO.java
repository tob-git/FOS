package com.foodapp.dao;

import com.foodapp.model.Menu;
import com.foodapp.model.Menu.MenuStatus;
import com.foodapp.model.MenuItem;
import com.foodapp.model.MenuItem.MenuItemCategory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    private static final String SELECT_ALL_MENUS = "SELECT * FROM menus ORDER BY restaurant_slug, name";
    private static final String SELECT_MENU_BY_ID = "SELECT * FROM menus WHERE id = ?";
    private static final String SELECT_MENUS_BY_RESTAURANT = "SELECT * FROM menus WHERE restaurant_slug = ? ORDER BY name";
    private static final String INSERT_MENU = "INSERT INTO menus (name, description, status, restaurant_slug, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_MENU = "UPDATE menus SET name = ?, description = ?, status = ?, updated_at = ? WHERE id = ?";
    private static final String DELETE_MENU = "DELETE FROM menus WHERE id = ?";
    
    private static final String SELECT_ALL_MENU_ITEMS = "SELECT * FROM menu_items ORDER BY menu_id, category, name";
    private static final String SELECT_MENU_ITEM_BY_ID = "SELECT * FROM menu_items WHERE id = ?";
    private static final String SELECT_MENU_ITEMS_BY_MENU = "SELECT * FROM menu_items WHERE menu_id = ? ORDER BY category, name";
    private static final String INSERT_MENU_ITEM = "INSERT INTO menu_items (name, description, price, image_url, category, is_available, menu_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_MENU_ITEM = "UPDATE menu_items SET name = ?, description = ?, price = ?, image_url = ?, category = ?, is_available = ?, updated_at = ? WHERE id = ?";
    private static final String DELETE_MENU_ITEM = "DELETE FROM menu_items WHERE id = ?";
    private static final String SEARCH_MENU_ITEMS = "SELECT * FROM menu_items WHERE name LIKE ? OR description LIKE ? ORDER BY menu_id, category, name";

    // Menu methods
    public List<Menu> findAllMenus() throws SQLException {
        List<Menu> menus = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_MENUS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Menu menu = mapResultSetToMenu(rs);
                menus.add(menu);
            }
        }
        
        return menus;
    }
    
    public Menu findMenuById(int id) throws SQLException {
        Menu menu = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MENU_BY_ID)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    menu = mapResultSetToMenu(rs);
                }
            }
        }
        
        return menu;
    }
    
    public List<Menu> findMenusByRestaurant(String restaurantSlug) throws SQLException {
        List<Menu> menus = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MENUS_BY_RESTAURANT)) {
            
            stmt.setString(1, restaurantSlug);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Menu menu = mapResultSetToMenu(rs);
                    menus.add(menu);
                }
            }
        }
        
        return menus;
    }
    
    public int insertMenu(Menu menu) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MENU, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, menu.name());
            stmt.setString(2, menu.description());
            stmt.setString(3, menu.status().toString());
            stmt.setString(4, menu.restaurantSlug());
            stmt.setObject(5, menu.createdAt());
            stmt.setObject(6, menu.updatedAt());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating menu failed, no ID obtained.");
                }
            }
        }
    }
    
    public void updateMenu(Menu menu) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MENU)) {
            
            stmt.setString(1, menu.name());
            stmt.setString(2, menu.description());
            stmt.setString(3, menu.status().toString());
            stmt.setObject(4, menu.updatedAt());
            stmt.setInt(5, menu.id());
            
            stmt.executeUpdate();
        }
    }
    
    public void deleteMenu(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_MENU)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // MenuItem methods
    public List<MenuItem> findAllMenuItems() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_MENU_ITEMS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                MenuItem menuItem = mapResultSetToMenuItem(rs);
                menuItems.add(menuItem);
            }
        }
        
        return menuItems;
    }
    
    public MenuItem findMenuItemById(int id) throws SQLException {
        MenuItem menuItem = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MENU_ITEM_BY_ID)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    menuItem = mapResultSetToMenuItem(rs);
                }
            }
        }
        
        return menuItem;
    }
    
    public List<MenuItem> findMenuItemsByMenu(int menuId) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MENU_ITEMS_BY_MENU)) {
            
            stmt.setInt(1, menuId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem menuItem = mapResultSetToMenuItem(rs);
                    menuItems.add(menuItem);
                }
            }
        }
        
        return menuItems;
    }
    
    public int insertMenuItem(MenuItem menuItem) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MENU_ITEM, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
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
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating menu item failed, no ID obtained.");
                }
            }
        }
    }
    
    public void updateMenuItem(MenuItem menuItem) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MENU_ITEM)) {
            
            stmt.setString(1, menuItem.name());
            stmt.setString(2, menuItem.description());
            stmt.setBigDecimal(3, menuItem.price());
            stmt.setString(4, menuItem.imageUrl());
            stmt.setString(5, menuItem.category().toString());
            stmt.setBoolean(6, menuItem.isAvailable());
            stmt.setObject(7, menuItem.updatedAt());
            stmt.setInt(8, menuItem.id());
            
            stmt.executeUpdate();
        }
    }
    
    public void deleteMenuItem(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_MENU_ITEM)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<MenuItem> searchMenuItems(String searchText) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_MENU_ITEMS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem menuItem = mapResultSetToMenuItem(rs);
                    menuItems.add(menuItem);
                }
            }
        }
        
        return menuItems;
    }
    
    private Menu mapResultSetToMenu(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        MenuStatus status = MenuStatus.fromString(rs.getString("status"));
        String restaurantSlug = rs.getString("restaurant_slug");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new Menu(
            id,
            name,
            description,
            status,
            restaurantSlug,
            createdAt,
            updatedAt
        );
    }
    
    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal price = rs.getBigDecimal("price");
        String imageUrl = rs.getString("image_url");
        MenuItemCategory category = MenuItemCategory.fromString(rs.getString("category"));
        boolean isAvailable = rs.getBoolean("is_available");
        int menuId = rs.getInt("menu_id");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new MenuItem(
            id,
            name,
            description,
            price,
            imageUrl,
            category,
            isAvailable,
            menuId,
            createdAt,
            updatedAt
        );
    }
} 