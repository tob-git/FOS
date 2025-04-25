package com.foodapp.model;

import java.math.BigDecimal;

public record OrderItem(
    int id,
    String orderCode,
    int menuItemId,
    String menuItemName,
    int quantity,
    BigDecimal priceAtOrderTime,
    String specialInstructions
) {
    public BigDecimal getSubtotal() {
        return priceAtOrderTime.multiply(BigDecimal.valueOf(quantity));
    }
    
    public String getFormattedPrice() {
        return "$" + priceAtOrderTime.toString();
    }
    
    public String getFormattedSubtotal() {
        return "$" + getSubtotal().toString();
    }
} 