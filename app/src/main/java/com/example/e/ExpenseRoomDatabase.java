package com.example.e;

import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.e.Converters;

@Database(entities = {ExpenseEntity.class, CategoryEntity.class, BudgetAlertEntity .class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class ExpenseRoomDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetAlertDao budgetAlertDao();

    private static ExpenseRoomDatabase INSTANCE;

    public static ExpenseRoomDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ExpenseRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ExpenseRoomDatabase.class, "expense_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}