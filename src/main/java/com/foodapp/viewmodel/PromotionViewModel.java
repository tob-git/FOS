package com.foodapp.viewmodel;

import com.foodapp.dao.PromotionDAO;
import com.foodapp.dao.RestaurantDAO;
import com.foodapp.model.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class PromotionViewModel {
    private final PromotionDAO promotionDAO;
    private final RestaurantDAO restaurantDAO;
    private final ObservableList<Promotion> promotions;

    public PromotionViewModel() {
        try {
            this.promotionDAO = new PromotionDAO();
            this.restaurantDAO = new RestaurantDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }
        this.promotions = FXCollections.observableArrayList();
    }

    public ObservableList<Promotion> getPromotions() {
        return promotions;
    }

    public void loadPromotions() throws SQLException {
        List<Promotion> promotionsList = promotionDAO.findAll();
        promotions.clear();
        promotions.addAll(promotionsList);
    }

    public void searchPromotions(String searchText) throws SQLException {
        List<Promotion> searchResults = promotionDAO.search(searchText);
        promotions.clear();
        promotions.addAll(searchResults);
    }

    public void addPromotion(Promotion promotion) throws SQLException {
        promotionDAO.insert(promotion);
        loadPromotions();
    }

    public void updatePromotion(Promotion promotion) throws SQLException {
        promotionDAO.update(promotion);
        loadPromotions();
    }

    public void deletePromotion(String code) throws SQLException {
        promotionDAO.delete(code);
        loadPromotions();
    }
    
    public List<String> loadRestaurantSlugs() throws SQLException {
        return restaurantDAO.findAllSlugs();
    }
} 