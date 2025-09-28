package com.example.fintrack;

import android.app.DatePickerDialog;
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
import com.example.fintrack.models.Category;
import com.example.fintrack.models.Transaction;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleButtonGroup;
    private TextInputEditText etAmount, etDate, etDescription;
    private AutoCompleteTextView actvCategory, actvAccount;
    private Button btnSaveTransaction, btnDeleteTransaction;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CollectionReference accountsRef, transactionsRef, budgetsRef;

    private final List<Account> userAccounts = new ArrayList<>();
    private final List<String> accountNames = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();

    private Transaction originalTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        originalTransaction = (Transaction) getIntent().getSerializableExtra("transaction");
        if (originalTransaction == null) {
            Toast.makeText(this, "Error: Transaction data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initFirebase();
        initViews();
        setupListeners();

        loadAccountsAndCategories();
        populateFieldsWithData();
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        String userId = currentUser.getUid();
        accountsRef = db.collection("users").document(userId).collection("accounts");
        transactionsRef = db.collection("users").document(userId).collection("transactions");
        budgetsRef = db.collection("users").document(userId).collection("budgets");
    }

    private void initViews() {
        toggleButtonGroup = findViewById(R.id.toggle_button_group);
        etAmount = findViewById(R.id.et_amount);
        actvCategory = findViewById(R.id.actv_category);
        actvAccount = findViewById(R.id.actv_account);
        etDate = findViewById(R.id.et_date);
        etDescription = findViewById(R.id.et_description);
        btnSaveTransaction = findViewById(R.id.btn_save_transaction);
        btnDeleteTransaction = findViewById(R.id.btn_delete_transaction);
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveTransaction.setOnClickListener(v -> saveChanges());
        btnDeleteTransaction.setOnClickListener(v -> confirmAndDelete());
    }

    private void loadAccountsAndCategories() {
        // In AddTransactionActivity.java
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

    private void populateFieldsWithData() {
        if ("Income".equals(originalTransaction.getType())) {
            toggleButtonGroup.check(R.id.btn_income);
        } else {
            toggleButtonGroup.check(R.id.btn_expense);
        }
        etAmount.setText(String.valueOf(originalTransaction.getAmount()));
        actvCategory.setText(originalTransaction.getCategory(), false);
        actvAccount.setText(originalTransaction.getAccountName(), false);
        etDescription.setText(originalTransaction.getDescription());

        selectedDate.setTime(originalTransaction.getDate());
        updateDateInView();
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

    private void saveChanges() {
        String type = toggleButtonGroup.getCheckedButtonId() == R.id.btn_income ? "Income" : "Expense";
        String amountStr = etAmount.getText().toString();
        String category = actvCategory.getText().toString();
        String accountName = actvAccount.getText().toString();
        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category) || TextUtils.isEmpty(accountName)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        double newAmount = Double.parseDouble(amountStr);

        WriteBatch batch = db.batch();

        double oldAmount = originalTransaction.getAmount();
        String oldType = originalTransaction.getType();
        String oldAccountId = originalTransaction.getAccountId();
        DocumentReference oldAccountRef = accountsRef.document(oldAccountId);

        if ("Income".equals(oldType)) {
            batch.update(oldAccountRef, "balance", FieldValue.increment(-oldAmount));
        } else {
            batch.update(oldAccountRef, "balance", FieldValue.increment(oldAmount));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String oldMonth = sdf.format(originalTransaction.getDate());
            DocumentReference oldBudgetRef = budgetsRef.document(originalTransaction.getCategory() + "_" + oldMonth);
            batch.update(oldBudgetRef, "spent", FieldValue.increment(-oldAmount));
        }

        Account newSelectedAccount = userAccounts.stream().filter(acc -> acc.getName().equals(accountName)).findFirst().orElse(null);
        if (newSelectedAccount == null) {
            Toast.makeText(this, "Selected account not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference newAccountRef = accountsRef.document(newSelectedAccount.getDocumentId());

        if ("Income".equals(type)) {
            batch.update(newAccountRef, "balance", FieldValue.increment(newAmount));
        } else {
            batch.update(newAccountRef, "balance", FieldValue.increment(-newAmount));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String newMonth = sdf.format(selectedDate.getTime());
            DocumentReference newBudgetRef = budgetsRef.document(category + "_" + newMonth);
            batch.update(newBudgetRef, "spent", FieldValue.increment(newAmount));
        }

        DocumentReference transactionRef = transactionsRef.document(originalTransaction.getDocumentId());
        batch.update(transactionRef, "type", type);
        batch.update(transactionRef, "amount", newAmount);
        batch.update(transactionRef, "category", category);
        batch.update(transactionRef, "accountId", newSelectedAccount.getDocumentId());
        batch.update(transactionRef, "accountName", newSelectedAccount.getName());
        batch.update(transactionRef, "description", etDescription.getText().toString());
        batch.update(transactionRef, "date", selectedDate.getTime());

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to update transaction: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void confirmAndDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteTransaction() {
        WriteBatch batch = db.batch();

        double oldAmount = originalTransaction.getAmount();
        String oldType = originalTransaction.getType();
        String oldAccountId = originalTransaction.getAccountId();
        DocumentReference oldAccountRef = accountsRef.document(oldAccountId);

        if ("Income".equals(oldType)) {
            batch.update(oldAccountRef, "balance", FieldValue.increment(-oldAmount));
        } else {
            batch.update(oldAccountRef, "balance", FieldValue.increment(oldAmount));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String oldMonth = sdf.format(originalTransaction.getDate());
            DocumentReference oldBudgetRef = budgetsRef.document(originalTransaction.getCategory() + "_" + oldMonth);
            batch.update(oldBudgetRef, "spent", FieldValue.increment(-oldAmount));
        }

        DocumentReference transactionRef = transactionsRef.document(originalTransaction.getDocumentId());
        batch.delete(transactionRef);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show());
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