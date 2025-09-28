package com.example.fintrack.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.adapters.BudgetAdapter;
import com.example.fintrack.models.Budget;
import com.example.fintrack.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetsFragment extends Fragment {

    private RecyclerView rvBudgets;
    private BudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private TextView tvNoBudgets;
    private FloatingActionButton fabAddBudget;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference budgetsRef;
    private String currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        initFirebase();
        initViews(view);
        setupRecyclerView();
        fetchBudgets();

        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        return view;
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle not logged in case if necessary
            return;
        }
        String userId = currentUser.getUid();
        budgetsRef = db.collection("users").document(userId).collection("budgets");
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }

    private void initViews(View view) {
        rvBudgets = view.findViewById(R.id.rv_budgets);
        tvNoBudgets = view.findViewById(R.id.tv_no_budgets);
        fabAddBudget = view.findViewById(R.id.fab_add_budget);
    }

    private void setupRecyclerView() {
        budgetList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(getContext(), budgetList);
        rvBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void fetchBudgets() {
        if (currentUser == null) return;
        budgetsRef.whereEqualTo("month", currentMonth)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle error
                        return;
                    }
                    if (isAdded() && value != null) { // Check if fragment is still attached
                        budgetList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            budgetList.add(doc.toObject(Budget.class));
                        }
                        budgetAdapter.notifyDataSetChanged();
                        tvNoBudgets.setVisibility(budgetList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showAddBudgetDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_budget, null);

        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actv_budget_category);
        EditText etLimit = dialogView.findViewById(R.id.et_budget_limit);

        // THIS IS THE UPDATED PART: Fetch categories from Firestore for the dropdown
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : queryDocumentSnapshots.toObjects(Category.class)) {
                        categoryNames.add(category.getName());
                    }
                    if (isAdded()) { // Ensure fragment is still valid
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                        actvCategory.setAdapter(adapter);
                    }
                });

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    String category = actvCategory.getText().toString();
                    String limitStr = etLimit.getText().toString();

                    if (TextUtils.isEmpty(category) || TextUtils.isEmpty(limitStr)) {
                        Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double limit = Double.parseDouble(limitStr);
                    Budget newBudget = new Budget(category, limit, 0.0, currentMonth);

                    // We use a combined key to ensure a category can only have one budget per month
                    budgetsRef.document(category + "_" + currentMonth).set(newBudget)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Budget set successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error setting budget", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }
}