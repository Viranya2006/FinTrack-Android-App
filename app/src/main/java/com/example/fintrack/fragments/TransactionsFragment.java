package com.example.fintrack.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.AddTransactionActivity;
import com.example.fintrack.EditTransactionActivity;
import com.example.fintrack.R;
import com.example.fintrack.adapters.TransactionAdapter;
import com.example.fintrack.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener, SearchView.OnQueryTextListener {

    private enum SortMode {
        NEWEST, OLDEST, AMOUNT_HIGH, AMOUNT_LOW
    }

    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private final List<Transaction> displayedTransactionList = new ArrayList<>();
    private final List<Transaction> allTransactions = new ArrayList<>();

    private ProgressBar progressBar;
    private TextView tvNoTransactions;
    private SearchView searchView;
    private ImageView ivAddTransactionToolbar;

    private SortMode currentSortMode = SortMode.NEWEST;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        initViews(view);
        setupRecyclerView();
        fetchTransactions();
        setupListeners(view);

        return view;
    }

    private void initViews(View view) {
        rvTransactions = view.findViewById(R.id.rv_transactions);
        progressBar = view.findViewById(R.id.progress_bar);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);
        searchView = view.findViewById(R.id.search_view);
        ivAddTransactionToolbar = view.findViewById(R.id.iv_add_transaction_toolbar);
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(getContext(), displayedTransactionList, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners(View view) {
        searchView.setOnQueryTextListener(this);

        ivAddTransactionToolbar.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddTransactionActivity.class));
        });

        // THIS IS THE NEW PART: Connect the Filter button to our sorting dialog
        view.findViewById(R.id.btn_filter).setOnClickListener(v -> {
            showSortDialog();
        });
    }

    private void fetchTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .addSnapshotListener((value, error) -> {
                    if (isAdded() && value != null) {
                        progressBar.setVisibility(View.GONE);
                        allTransactions.clear();
                        allTransactions.addAll(value.toObjects(Transaction.class));
                        applyFiltersAndSorting();
                    } else if (isAdded() && error != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error fetching data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- THIS IS THE NEW METHOD THAT SHOWS THE SORTING DIALOG ---
    private void showSortDialog() {
        final String[] sortOptions = {"Newest First", "Oldest First", "Amount (High to Low)", "Amount (Low to High)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort By");

        // The index of the currently selected sort mode
        int checkedItem = currentSortMode.ordinal();

        builder.setSingleChoiceItems(sortOptions, checkedItem, (dialog, which) -> {
            // 'which' is the index of the newly selected item
            currentSortMode = SortMode.values()[which];
            applyFiltersAndSorting();
            dialog.dismiss(); // Close the dialog immediately after selection
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void applyFiltersAndSorting() {
        List<Transaction> filteredList = new ArrayList<>();
        if (currentSearchQuery.isEmpty()) {
            filteredList.addAll(allTransactions);
        } else {
            String lowerCaseQuery = currentSearchQuery.toLowerCase();
            for (Transaction transaction : allTransactions) {
                if ((transaction.getCategory() != null && transaction.getCategory().toLowerCase().contains(lowerCaseQuery)) ||
                        (transaction.getDescription() != null && transaction.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                        String.valueOf(transaction.getAmount()).contains(lowerCaseQuery)) {
                    filteredList.add(transaction);
                }
            }
        }

        switch (currentSortMode) {
            case NEWEST:
                Collections.sort(filteredList, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                break;
            case OLDEST:
                Collections.sort(filteredList, (t1, t2) -> t1.getDate().compareTo(t2.getDate()));
                break;
            case AMOUNT_HIGH:
                Collections.sort(filteredList, (t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
                break;
            case AMOUNT_LOW:
                Collections.sort(filteredList, (t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
                break;
        }

        displayedTransactionList.clear();
        displayedTransactionList.addAll(filteredList);
        transactionAdapter.notifyDataSetChanged();

        tvNoTransactions.setVisibility(displayedTransactionList.isEmpty() ? View.VISIBLE : View.GONE);
        rvTransactions.setVisibility(displayedTransactionList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(getActivity(), EditTransactionActivity.class);
        intent.putExtra("transaction", transaction);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentSearchQuery = newText;
        applyFiltersAndSorting();
        return true;
    }
}