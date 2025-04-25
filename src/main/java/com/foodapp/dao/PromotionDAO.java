package com.foodapp.dao;

import com.foodapp.model.Promotion;
import com.foodapp.model.Promotion.PromotionStatus;
import com.foodapp.model.Promotion.PromotionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {

    private static final String SELECT_ALL_PROMOTIONS = "SELECT * FROM promotions ORDER BY created_at DESC";
    private static final String SELECT_PROMOTION_BY_CODE = "SELECT * FROM promotions WHERE code = ?";
    private static final String INSERT_PROMOTION = "INSERT INTO promotions (code, description, type, value, start_date, end_date, status, restaurant_slug, usage_limit, usage_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PROMOTION = "UPDATE promotions SET description = ?, type = ?, value = ?, start_date = ?, end_date = ?, status = ?, restaurant_slug = ?, usage_limit = ?, usage_count = ?, updated_at = ? WHERE code = ?";
    private static final String DELETE_PROMOTION = "DELETE FROM promotions WHERE code = ?";
    private static final String SELECT_ACTIVE_PROMOTIONS = "SELECT * FROM promotions WHERE status = 'ACTIVE' AND start_date <= NOW() AND end_date >= NOW()";
    private static final String SELECT_PROMOTIONS_BY_RESTAURANT = "SELECT * FROM promotions WHERE restaurant_slug = ? ORDER BY created_at DESC";
    private static final String SEARCH_PROMOTIONS = "SELECT * FROM promotions WHERE code LIKE ? OR description LIKE ? OR restaurant_slug LIKE ? ORDER BY created_at DESC";
    private static final String INCREMENT_USAGE_COUNT = "UPDATE promotions SET usage_count = usage_count + 1, updated_at = ? WHERE code = ?";

    public List<Promotion> findAll() throws SQLException {
        List<Promotion> promotions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_PROMOTIONS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Promotion promotion = mapResultSetToPromotion(rs);
                promotions.add(promotion);
            }
        }
        
        return promotions;
    }
    
    public Promotion findByCode(String code) throws SQLException {
        Promotion promotion = null;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PROMOTION_BY_CODE)) {
            
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    promotion = mapResultSetToPromotion(rs);
                }
            }
        }
        
        return promotion;
    }
    
    public void insert(Promotion promotion) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PROMOTION)) {
            
            stmt.setString(1, promotion.code());
            stmt.setString(2, promotion.description());
            stmt.setString(3, promotion.type().toString());
            stmt.setBigDecimal(4, promotion.value());
            stmt.setObject(5, promotion.startDate());
            stmt.setObject(6, promotion.endDate());
            stmt.setString(7, promotion.status().toString());
            stmt.setString(8, promotion.restaurantSlug());
            stmt.setInt(9, promotion.usageLimit());
            stmt.setInt(10, promotion.usageCount());
            stmt.setObject(11, promotion.createdAt());
            stmt.setObject(12, promotion.updatedAt());
            
            stmt.executeUpdate();
        }
    }
    
    public void update(Promotion promotion) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PROMOTION)) {
            
            stmt.setString(1, promotion.description());
            stmt.setString(2, promotion.type().toString());
            stmt.setBigDecimal(3, promotion.value());
            stmt.setObject(4, promotion.startDate());
            stmt.setObject(5, promotion.endDate());
            stmt.setString(6, promotion.status().toString());
            stmt.setString(7, promotion.restaurantSlug());
            stmt.setInt(8, promotion.usageLimit());
            stmt.setInt(9, promotion.usageCount());
            stmt.setObject(10, promotion.updatedAt());
            stmt.setString(11, promotion.code());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String code) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PROMOTION)) {
            
            stmt.setString(1, code);
            stmt.executeUpdate();
        }
    }
    
    public List<Promotion> findActive() throws SQLException {
        List<Promotion> promotions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_PROMOTIONS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Promotion promotion = mapResultSetToPromotion(rs);
                promotions.add(promotion);
            }
        }
        
        return promotions;
    }
    
    public List<Promotion> findByRestaurant(String restaurantSlug) throws SQLException {
        List<Promotion> promotions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PROMOTIONS_BY_RESTAURANT)) {
            
            stmt.setString(1, restaurantSlug);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Promotion promotion = mapResultSetToPromotion(rs);
                    promotions.add(promotion);
                }
            }
        }
        
        return promotions;
    }
    
    public List<Promotion> search(String searchText) throws SQLException {
        List<Promotion> promotions = new ArrayList<>();
        String searchPattern = "%" + searchText + "%";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_PROMOTIONS)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Promotion promotion = mapResultSetToPromotion(rs);
                    promotions.add(promotion);
                }
            }
        }
        
        return promotions;
    }
    
    public void incrementUsageCount(String code) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INCREMENT_USAGE_COUNT)) {
            
            stmt.setObject(1, LocalDateTime.now());
            stmt.setString(2, code);
            
            stmt.executeUpdate();
        }
    }
    
    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        String code = rs.getString("code");
        String description = rs.getString("description");
        PromotionType type = PromotionType.fromString(rs.getString("type"));
        BigDecimal value = rs.getBigDecimal("value");
        LocalDateTime startDate = rs.getObject("start_date", LocalDateTime.class);
        LocalDateTime endDate = rs.getObject("end_date", LocalDateTime.class);
        PromotionStatus status = PromotionStatus.fromString(rs.getString("status"));
        String restaurantSlug = rs.getString("restaurant_slug");
        int usageLimit = rs.getInt("usage_limit");
        int usageCount = rs.getInt("usage_count");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);
        
        return new Promotion(
            code,
            description,
            type,
            value,
            startDate,
            endDate,
            status,
            restaurantSlug,
            usageLimit,
            usageCount,
            createdAt,
            updatedAt
        );
    }
} 