package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuItem(
    int id,
    String name,
    String description,
    BigDecimal price,
    String imageUrl,
    MenuItemCategory category,
    boolean isAvailable,
    int menuId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum MenuItemCategory {
        APPETIZER, MAIN_COURSE, DESSERT, BEVERAGE, SIDE, SPECIAL;
        
        public static MenuItemCategory fromString(String category) {
            return switch (category.toUpperCase()) {
                case "APPETIZER" -> APPETIZER;
                case "MAIN_COURSE", "MAIN", "ENTREE" -> MAIN_COURSE;
                case "DESSERT" -> DESSERT;
                case "BEVERAGE", "DRINK" -> BEVERAGE;
                case "SIDE", "SIDE_DISH" -> SIDE;
                case "SPECIAL" -> SPECIAL;
                default -> throw new IllegalArgumentException("Unknown category: " + category);
            };
        }
    }
    
    public String getFormattedPrice() {
        return "$" + price.toString();
    }
} 