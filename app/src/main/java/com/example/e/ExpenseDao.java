package com.example.e;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insert(ExpenseEntity expense);

    @Update
    int update(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<ExpenseEntity> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE category = :category")
    List<ExpenseEntity> searchExpensesByCategory(String category);

    @Query("SELECT SUM(CAST(amount AS DOUBLE)) FROM expenses WHERE category = :category AND date LIKE :monthPattern")
    Double getTotalSpendingByCategory(String category, String monthPattern);

    @Query("SELECT SUM(CAST(amount AS DOUBLE)) FROM expenses WHERE date LIKE :monthPattern")
    Double getTotalSpendingForMonth(String monthPattern);
}