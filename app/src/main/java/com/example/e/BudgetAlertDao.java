package com.example.e;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BudgetAlertDao {
    @Insert
    void insert(BudgetAlertEntity alert);

    @Query("SELECT * FROM budget_alerts ORDER BY timestamp DESC")
    List<BudgetAlertEntity> getAllAlerts();

    @Query("SELECT * FROM budget_alerts WHERE category = :category AND month = :month ORDER BY timestamp DESC")
    List<BudgetAlertEntity> getAlertsByCategoryAndMonth(String category, String month);
}