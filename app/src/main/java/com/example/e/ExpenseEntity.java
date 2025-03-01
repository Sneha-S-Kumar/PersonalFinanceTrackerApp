package com.example.e;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;
    private String amount;
    private String notes;
    private String date;

    @TypeConverters(UriConverter.class)
    private Uri imagePath;

    public ExpenseEntity(String category, String amount, String notes, String date, Uri imagePath) {
        this.category = category;
        this.amount = amount;
        this.notes = notes;
        this.date = date;
        this.imagePath = imagePath;
    }

    public ExpenseEntity() {

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Uri getImagePath() {
        return imagePath;
    }

    public void setImagePath(Uri imagePath) {
        this.imagePath = imagePath;
    }

}