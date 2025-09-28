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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.adapters.AccountAdapter;
import com.example.fintrack.models.Account;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.content.Intent;


import java.util.ArrayList;
import java.util.List;

public class ManageAccountsActivity extends AppCompatActivity implements AccountAdapter.OnAccountClickListener {

    private RecyclerView rvAccounts;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;

    private TextInputEditText etAccountName;
    private AutoCompleteTextView actvAccountType;
    private Button btnAddAccount;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference accountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etAccountName = findViewById(R.id.et_account_name);
        actvAccountType = findViewById(R.id.actv_account_type);
        btnAddAccount = findViewById(R.id.btn_add_account);

        setupRecyclerView();
        setupAccountTypeDropdown();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            accountsRef = db.collection("users").document(currentUser.getUid()).collection("accounts");
            fetchAccounts();
        }

        btnAddAccount.setOnClickListener(v -> addAccount());
    }

    private void setupRecyclerView() {
        rvAccounts = findViewById(R.id.rv_accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        accountList = new ArrayList<>();
        // Pass 'this' as the listener
        accountAdapter = new AccountAdapter(this, accountList, this);
        rvAccounts.setAdapter(accountAdapter);
    }

    private void setupAccountTypeDropdown() {
        String[] accountTypes = getResources().getStringArray(R.array.account_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountTypes);
        actvAccountType.setAdapter(adapter);
    }

    private void fetchAccounts() {
        accountsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error fetching accounts", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                accountList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Account account = doc.toObject(Account.class);
                    accountList.add(account);
                }
                accountAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addAccount() {
        String name = etAccountName.getText().toString().trim();
        String type = actvAccountType.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etAccountName.setError("Account name is required");
            return;
        }
        if (TextUtils.isEmpty(type)) {
            actvAccountType.setError("Account type is required");
            return;
        }

        // For MVP, new accounts start with a 0 balance.
        Account newAccount = new Account(name, 0.0, type);

        accountsRef.add(newAccount)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ManageAccountsActivity.this, "Account added successfully", Toast.LENGTH_SHORT).show();
                    etAccountName.setText("");
                    actvAccountType.setText("");
                    etAccountName.clearFocus();
                })
                .addOnFailureListener(e -> Toast.makeText(ManageAccountsActivity.this, "Error adding account", Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onEditClick(Account account) {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("account", account);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Account account) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    accountsRef.document(account.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}