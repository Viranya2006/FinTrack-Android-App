package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.models.Category;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
        progressBar = findViewById(R.id.progress_bar);

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // ... (All validation code is the same) ...
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) { Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show(); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters."); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match."); return; }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Create user document first
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("currency", "LKR");
                            user.put("profile_updated_at", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // AFTER user document is created, create categories
                                        createDefaultCategories(userId);
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "Error storing user data.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createDefaultCategories(String userId) {
        CollectionReference categoriesRef = db.collection("users").document(userId).collection("categories");
        Map<String, String> defaultCategories = new LinkedHashMap<>();
        defaultCategories.put("Groceries", "Food");
        defaultCategories.put("Transport", "Transport");
        defaultCategories.put("Movies", "Entertainment");
        defaultCategories.put("Electricity", "Utilities");
        defaultCategories.put("Gym", "Health");
        defaultCategories.put("Clothing", "Shopping");
        defaultCategories.put("Flights", "Travel");
        defaultCategories.put("Restaurants", "Food");
        defaultCategories.put("Salary", "Income");

        for (Map.Entry<String, String> entry : defaultCategories.entrySet()) {
            Category newCategory = new Category(entry.getKey(), entry.getValue());
            // We don't care if one fails, we just want to add them.
            // This is more robust than a batch for this specific setup task.
            categoriesRef.document(entry.getKey()).set(newCategory);
        }

        // Don't wait for all categories to finish. Navigate the user immediately.
        progressBar.setVisibility(View.GONE);
        Toast.makeText(RegisterActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}