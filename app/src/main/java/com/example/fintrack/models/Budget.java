package com.example.fintrack.models;

import com.google.firebase.firestore.DocumentId;

public class Budget {
    @DocumentId
    private String category; // Using category name as the document ID
    private double limit;
    private double spent;
    private String month; // Format: "yyyy-MM"

    // Required empty constructor for Firestore
    public Budget() {}

    public Budget(String category, double limit, double spent, String month) {
        this.category = category;
        this.limit = limit;
        this.spent = spent;
        this.month = month;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }

    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}