package com.example.fintrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.fintrack.models.Category;
import com.google.firebase.firestore.FieldValue; // Add this import


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fintrack.models.Account;
import com.example.fintrack.models.Transaction;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleButtonGroup;
    private TextInputEditText etAmount, etDate, etDescription;
    private AutoCompleteTextView actvCategory, actvAccount;
    private Button btnSaveTransaction;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference accountsRef, transactionsRef;

    private final List<Account> userAccounts = new ArrayList<>();
    private final List<String> accountNames = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Handle user not logged in case
            finish();
            return;
        }

        String userId = currentUser.getUid();
        accountsRef = db.collection("users").document(userId).collection("accounts");
        transactionsRef = db.collection("users").document(userId).collection("transactions");

        initViews();
        setupListeners();

        loadAccounts();
        loadCategories();
    }

    private void initViews() {
        toggleButtonGroup = findViewById(R.id.toggle_button_group);
        etAmount = findViewById(R.id.et_amount);
        actvCategory = findViewById(R.id.actv_category);
        actvAccount = findViewById(R.id.actv_account);
        etDate = findViewById(R.id.et_date);
        etDescription = findViewById(R.id.et_description);
        btnSaveTransaction = findViewById(R.id.btn_save_transaction);

        // Set default selection
        toggleButtonGroup.check(R.id.btn_expense);
        updateDateInView();
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void loadAccounts() {
        accountsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            userAccounts.clear();
            accountNames.clear();
            for (Account account : queryDocumentSnapshots.toObjects(Account.class)) {
                userAccounts.add(account);
                accountNames.add(account.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountNames);
            actvAccount.setAdapter(adapter);
        });
    }

    // In AddTransactionActivity.java
    private void loadCategories() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : queryDocumentSnapshots.toObjects(Category.class)) {
                        categoryNames.add(category.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
                    actvCategory.setAdapter(adapter);
                });
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        // --- 1. Get and Validate Data ---
        String type = toggleButtonGroup.getCheckedButtonId() == R.id.btn_income ? "Income" : "Expense";
        String amountStr = etAmount.getText().toString();
        String category = actvCategory.getText().toString();
        String accountName = actvAccount.getText().toString();
        String description = etDescription.getText().toString();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category) || TextUtils.isEmpty(accountName)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            etAmount.setError("Amount must be positive");
            return;
        }

        Account selectedAccount = null;
        for (Account acc : userAccounts) {
            if (acc.getName().equals(accountName)) {
                selectedAccount = acc;
                break;
            }
        }
        if (selectedAccount == null) {
            actvAccount.setError("Invalid account selected");
            return;
        }

        // --- 2. Prepare Data Objects ---
        double newBalance;
        if (type.equals("Income")) {
            newBalance = selectedAccount.getBalance() + amount;
        } else {
            newBalance = selectedAccount.getBalance() - amount;
        }

        Date transactionDate = selectedDate.getTime(); // This gets a Date object
        Transaction newTransaction = new Transaction(type, amount, category, selectedAccount.getDocumentId(), accountName, description, transactionDate, currentUser.getUid());


        // --- 3. Perform Firestore Batch Write ---
        WriteBatch batch = db.batch();

        // Operation 1: Create the new transaction
        DocumentReference newTransactionRef = transactionsRef.document();
        batch.set(newTransactionRef, newTransaction);

        // Operation 2: Update the account balance
        DocumentReference accountRef = accountsRef.document(selectedAccount.getDocumentId());
        batch.update(accountRef, "balance", newBalance);

        // Operation 3 (Conditional): Update budget if it's an expense
        if (type.equals("Expense")) {
            String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
            DocumentReference budgetRef = db.collection("users").document(currentUser.getUid())
                    .collection("budgets").document(category + "_" + currentMonth);

            // Use FieldValue.increment to safely add the expense amount to the 'spent' field.
            // This works even if multiple transactions happen at once.
            batch.update(budgetRef, "spent", FieldValue.increment(amount));
        }

        // Commit the batch
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(AddTransactionActivity.this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(AddTransactionActivity.this, "Error saving transaction: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
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