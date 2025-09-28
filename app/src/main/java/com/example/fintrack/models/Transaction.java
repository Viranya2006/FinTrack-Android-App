package com.example.fintrack.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.Date; // Import java.util.Date

public class Transaction implements Serializable {
    @DocumentId
    private String documentId;
    private String type;
    private double amount;
    private String category;
    private String accountId;
    private String accountName;
    private String description;
    private Date date; // CHANGED FROM Timestamp
    private String userId;

    public Transaction() {}

    // Constructor updated to accept Date
    public Transaction(String type, double amount, String category, String accountId, String accountName, String description, Date date, String userId) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.accountId = accountId;
        this.accountName = accountName;
        this.description = description;
        this.date = date;
        this.userId = userId;
    }

    // --- Getters and Setters updated for Date ---

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; } // CHANGED FROM Timestamp
    public void setDate(Date date) { this.date = date; } // CHANGED FROM Timestamp

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}