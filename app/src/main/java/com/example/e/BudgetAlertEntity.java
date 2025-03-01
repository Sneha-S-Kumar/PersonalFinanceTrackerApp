package com.example.e;



import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget_alerts")
public class BudgetAlertEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;
    private String month;
    private String message;
    private long timestamp;

    public BudgetAlertEntity(String category, String month, String message, long timestamp) {
        this.category = category;
        this.month = month;
        this.message = message;
        this.timestamp = timestamp;
    }

  
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
