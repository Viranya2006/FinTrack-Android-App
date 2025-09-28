package com.example.fintrack.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.adapters.TransactionAdapter;
import com.example.fintrack.models.Account;
import com.example.fintrack.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvWelcomeMessage, tvTotalBalance, tvMonthlyIncome, tvMonthlyExpense;
    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> recentTransactionList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initFirebase();

        if (currentUser != null) {
            loadUserProfile();
            fetchDashboardData();
        }

        return view;
    }

    private void initViews(View view) {
        tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvMonthlyIncome = view.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpense = view.findViewById(R.id.tv_monthly_expense);
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);

        recentTransactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), recentTransactionList, null); // Pass null as we don't need clicks here
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void loadUserProfile() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeMessage.setText("Welcome back, " + name);
                    }
                });
    }

    private void fetchDashboardData() {
        String userId = currentUser.getUid();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

        // 1. Fetch Total Balance from all accounts
        db.collection("users").document(userId).collection("accounts").addSnapshotListener((value, error) -> {
            if (isAdded() && value != null) {
                double totalBalance = 0.0;
                for (Account account : value.toObjects(Account.class)) {
                    totalBalance += account.getBalance();
                }
                tvTotalBalance.setText(currencyFormat.format(totalBalance));
            }
        });

        // 2. Fetch Transactions for the current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date endDate = calendar.getTime();

        db.collection("users").document(userId).collection("transactions")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .addSnapshotListener((value, error) -> {
                    if (isAdded() && value != null) {
                        double monthlyIncome = 0.0;
                        double monthlyExpense = 0.0;
                        Map<String, Float> expenseByCategory = new HashMap<>();

                        for (Transaction transaction : value.toObjects(Transaction.class)) {
                            if ("Income".equals(transaction.getType())) {
                                monthlyIncome += transaction.getAmount();
                            } else {
                                monthlyExpense += transaction.getAmount();
                                String category = transaction.getCategory();
                                expenseByCategory.put(category, expenseByCategory.getOrDefault(category, 0f) + (float) transaction.getAmount());
                            }
                        }

                        tvMonthlyIncome.setText(currencyFormat.format(monthlyIncome));
                        tvMonthlyExpense.setText(currencyFormat.format(monthlyExpense));

                        setupPieChart(expenseByCategory);
                        setupBarChart((float) monthlyIncome, (float) monthlyExpense);
                    }
                });

        // 3. Fetch Recent Transactions (last 5)
        db.collection("users").document(userId).collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (isAdded() && value != null) {
                        recentTransactionList.clear();
                        List<Transaction> allTransactions = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            allTransactions.add(doc.toObject(Transaction.class));
                        }
                        transactionAdapter.updateFullList(allTransactions); // Important for adapters with filtering
                        recentTransactionList.addAll(allTransactions);
                        transactionAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupPieChart(Map<String, Float> data) {
        if (!isAdded()) return;
        if (data == null || data.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expenses this month.");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupBarChart(float income, float expense) {
        if (!isAdded()) return;
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, income));
        entries.add(new BarEntry(1, expense));

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Overview");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.green_income), ContextCompat.getColor(getContext(), R.color.red_expense));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(Arrays.asList("Income", "Expense")));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(2);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}