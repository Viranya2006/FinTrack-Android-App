package com.example.fintrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fintrack.models.Account;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountActivity extends AppCompatActivity {

    private TextInputEditText etAccountName;
    private AutoCompleteTextView actvAccountType;
    private Button btnSaveChanges, btnDeleteAccount;

    private FirebaseFirestore db;
    private CollectionReference accountsRef;

    private Account originalAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        originalAccount = (Account) getIntent().getSerializableExtra("account");
        if (originalAccount == null) {
            Toast.makeText(this, "Error: Account data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initFirebase();
        initViews();
        setupListeners();

        setupAccountTypeDropdown();
        populateFields();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        accountsRef = db.collection("users").document(userId).collection("accounts");
    }

    private void initViews() {
        etAccountName = findViewById(R.id.et_account_name);
        actvAccountType = findViewById(R.id.actv_account_type);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);
    }

    private void setupListeners() {
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnDeleteAccount.setOnClickListener(v -> confirmAndDelete());
    }

    private void setupAccountTypeDropdown() {
        String[] accountTypes = getResources().getStringArray(R.array.account_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountTypes);
        actvAccountType.setAdapter(adapter);
    }

    private void populateFields() {
        etAccountName.setText(originalAccount.getName());
        actvAccountType.setText(originalAccount.getType(), false); // false to prevent filtering
    }

    private void saveChanges() {
        String newName = etAccountName.getText().toString().trim();
        String newType = actvAccountType.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newType)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference accountDoc = accountsRef.document(originalAccount.getDocumentId());
        accountDoc.update("name", newName, "type", newType)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating account", Toast.LENGTH_SHORT).show());
    }

    private void confirmAndDelete() {
        // Important: We should check if there are transactions associated with this account before deleting.
        // For MVP, we will keep it simple with a strong warning.
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? All associated transaction records will NOT be deleted but will lose their account link. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        accountsRef.document(originalAccount.getDocumentId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}