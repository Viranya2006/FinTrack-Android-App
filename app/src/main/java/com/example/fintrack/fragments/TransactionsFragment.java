package com.example.fintrack.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView; // Import SearchView
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.EditTransactionActivity;
import com.example.fintrack.R;
import com.example.fintrack.adapters.TransactionAdapter;
import com.example.fintrack.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

// Implement the new listener interface
public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener, SearchView.OnQueryTextListener {

    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView tvNoTransactions;
    private SearchView searchView; // Add SearchView variable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvTransactions = view.findViewById(R.id.rv_transactions);
        progressBar = view.findViewById(R.id.progress_bar);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);
        searchView = view.findViewById(R.id.search_view); // Find the SearchView

        setupRecyclerView();
        fetchTransactions();

        searchView.setOnQueryTextListener(this); // Set the listener

        return view;
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), transactionList, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void fetchTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoTransactions.setVisibility(View.GONE);
        rvTransactions.setVisibility(View.GONE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle case where user is not logged in
            progressBar.setVisibility(View.GONE);
            tvNoTransactions.setText("Please log in to see transactions.");
            tvNoTransactions.setVisibility(View.VISIBLE);
            return;
        }
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        if (error != null) return;

                        if (value != null) {
                            transactionList.clear();
                            List<Transaction> allTransactions = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : value) {
                                allTransactions.add(doc.toObject(Transaction.class));
                            }

                            // Update the adapter's full list and the displayed list
                            transactionAdapter.updateFullList(allTransactions);
                            transactionList.addAll(allTransactions);

                            transactionAdapter.notifyDataSetChanged();

                            if (transactionList.isEmpty()) {
                                tvNoTransactions.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            } else {
                                tvNoTransactions.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
    /**
     * This method is called when a transaction item is clicked in the RecyclerView.
     * It's part of the OnTransactionClickListener interface implementation.
     * @param transaction The transaction object that was clicked.
     */
    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(getActivity(), EditTransactionActivity.class);
        intent.putExtra("transaction", transaction);
        startActivity(intent);
    }@Override
    public boolean onQueryTextSubmit(String query) {
        // We filter on text change, so no action needed here
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // This is called every time the user types a character
        if (transactionAdapter != null) {
            transactionAdapter.filter(newText);
        }
        return true;
    }
}
