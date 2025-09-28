package com.example.fintrack.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.models.Budget;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final Context context;
    private final List<Budget> budgetList;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

    public BudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.budget_item_layout, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        holder.tvCategory.setText(budget.getCategory());
        holder.tvSpent.setText(currencyFormat.format(budget.getSpent()));
        holder.tvLimit.setText("of " + currencyFormat.format(budget.getLimit()));

        int progress = 0;
        if (budget.getLimit() > 0) {
            progress = (int) ((budget.getSpent() / budget.getLimit()) * 100);
        }
        holder.progressBar.setProgress(progress);

        // Change progress bar color based on percentage
        if (progress > 90) {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_expense)));
        } else if (progress > 75) {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_accent)));
        } else {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green)));
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvSpent, tvLimit;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_budget_category);
            tvSpent = itemView.findViewById(R.id.tv_budget_spent);
            tvLimit = itemView.findViewById(R.id.tv_budget_limit);
            progressBar = itemView.findViewById(R.id.progress_bar_budget);
        }
    }
}