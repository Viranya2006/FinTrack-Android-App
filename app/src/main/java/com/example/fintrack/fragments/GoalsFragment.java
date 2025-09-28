package com.example.fintrack.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.fintrack.adapters.SavingGoalAdapter;
import com.example.fintrack.models.SavingGoal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GoalsFragment extends Fragment implements SavingGoalAdapter.OnContributeClickListener {

    private RecyclerView rvGoals;
    private SavingGoalAdapter goalAdapter;
    private List<SavingGoal> goalList;
    private TextView tvNoGoals;
    private FloatingActionButton fabAddGoal;

    private CollectionReference goalsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false); // Make sure this layout exists and is correct

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        goalsRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings_goals");

        initViews(view);
        setupRecyclerView();
        fetchGoals();

        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());

        return view;
    }

    private void initViews(View view) {
        rvGoals = view.findViewById(R.id.rv_goals);
        tvNoGoals = view.findViewById(R.id.tv_no_goals);
        fabAddGoal = view.findViewById(R.id.fab_add_goal);
    }

    private void setupRecyclerView() {
        goalList = new ArrayList<>();
        goalAdapter = new SavingGoalAdapter(getContext(), goalList, this);
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGoals.setAdapter(goalAdapter);
    }

    private void fetchGoals() {
        goalsRef.orderBy("name", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                goalList.clear();
                goalList.addAll(value.toObjects(SavingGoal.class));
                goalAdapter.notifyDataSetChanged();
                tvNoGoals.setVisibility(goalList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_saving_goal, null);
        EditText etGoalName = dialogView.findViewById(R.id.et_goal_name);
        EditText etGoalTarget = dialogView.findViewById(R.id.et_goal_target);

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    String name = etGoalName.getText().toString();
                    String targetStr = etGoalTarget.getText().toString();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(targetStr)) return;

                    double target = Double.parseDouble(targetStr);
                    SavingGoal newGoal = new SavingGoal(name, target, 0.0);
                    goalsRef.add(newGoal).addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Goal Added!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).create().show();
    }

    @Override
    public void onContributeClick(SavingGoal goal) {
        showAddDepositDialog(goal);
    }

    private void showAddDepositDialog(SavingGoal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_deposit, null);
        EditText etDepositAmount = dialogView.findViewById(R.id.et_deposit_amount);
        TextView tvTitle = dialogView.findViewById(R.id.tv_deposit_title);
        tvTitle.setText("Contribute to " + goal.getName());

        builder.setView(dialogView)
                .setPositiveButton("Add", (dialog, id) -> {
                    String amountStr = etDepositAmount.getText().toString();
                    if (TextUtils.isEmpty(amountStr)) return;

                    double amount = Double.parseDouble(amountStr);
                    goalsRef.document(goal.getDocumentId()).update("savedAmount", FieldValue.increment(amount))
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Contribution added!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).create().show();
    }
}