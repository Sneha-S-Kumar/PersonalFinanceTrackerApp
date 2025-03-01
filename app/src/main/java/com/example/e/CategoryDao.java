package com.example.e;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(CategoryEntity category);

    @Query("SELECT category FROM categories")
    List<String> getAllCategoryNames();


    @Query("SELECT * FROM categories WHERE category = :name AND month = :month")
    CategoryEntity getCategoryByNameAndMonth(String name, String month);

    @Query("SELECT * FROM categories WHERE month = :month")
    List<CategoryEntity> getCategoriesByMonth(String month);

    @Query("UPDATE categories SET budget = :budget WHERE category = :category AND month = :month")
    int updateBudget(String category, String budget, String month);

    @Query("SELECT SUM(CAST(budget AS DOUBLE)) FROM categories WHERE month = :month")
    Double getTotalBudgetForMonth(String month);
}
