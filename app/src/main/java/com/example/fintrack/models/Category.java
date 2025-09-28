package com.example.fintrack.models;

import com.google.firebase.firestore.DocumentId;

public class Category {
    @DocumentId
    private String name;
    private String type; // NEW FIELD

    public Category() {}

    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; } // NEW GETTER
    public void setType(String type) { this.type = type; } // NEW SETTER
}