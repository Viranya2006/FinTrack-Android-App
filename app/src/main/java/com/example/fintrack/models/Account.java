package com.example.fintrack.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class Account implements Serializable {
    @DocumentId
    private String documentId;
    private String name;
    private double balance;
    private String type; // e.g., "Cash", "Bank", "Credit Card"

    // Required empty constructor for Firestore
    public Account() {}

    public Account(String name, double balance, String type) {
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}