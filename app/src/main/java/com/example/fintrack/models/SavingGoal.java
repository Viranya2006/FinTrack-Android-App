package com.example.fintrack.models;

import com.google.firebase.firestore.DocumentId;

public class SavingGoal {
    @DocumentId
    private String documentId;
    private String name;
    private double targetAmount;
    private double savedAmount;

    // Required empty constructor for Firestore
    public SavingGoal() {}

    public SavingGoal(String name, double targetAmount, double savedAmount) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }
}