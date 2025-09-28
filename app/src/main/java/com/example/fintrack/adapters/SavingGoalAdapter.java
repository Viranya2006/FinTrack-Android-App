package com.example.fintrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.models.SavingGoal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SavingGoalAdapter extends RecyclerView.Adapter<SavingGoalAdapter.GoalViewHolder> {

    private final Context context;
    private final List<SavingGoal> goalList;
    private final OnContributeClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

    public interface OnContributeClickListener {
        void onContributeClick(SavingGoal goal);
    }

    public SavingGoalAdapter(Context context, List<SavingGoal> goalList, OnContributeClickListener listener) {
        this.context = context;
        this.goalList = goalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saving_goal_item_layout, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        SavingGoal goal = goalList.get(position);
        holder.tvGoalName.setText(goal.getName());
        String amountsText = currencyFormat.format(goal.getSavedAmount()) + " / " + currencyFormat.format(goal.getTargetAmount());
        holder.tvGoalAmounts.setText(amountsText);

        int progress = 0;
        if (goal.getTargetAmount() > 0) {
            progress = (int) ((goal.getSavedAmount() / goal.getTargetAmount()) * 100);
        }
        holder.tvProgressPercent.setText(progress + "%");
        holder.progressBar.setProgress(progress);

        // Motivational Suggestion Logic
        if (goal.getSavedAmount() >= 20000 && goal.getSavedAmount() < 50000) {
            holder.tvSuggestion.setText("Suggestion: You could go on a weekend holiday!");
            holder.tvSuggestion.setVisibility(View.VISIBLE);
        } else if (goal.getSavedAmount() >= 50000) {
            holder.tvSuggestion.setText("Suggestion: You're well on your way to a major purchase!");
            holder.tvSuggestion.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestion.setVisibility(View.GONE);
        }

        holder.btnContribute.setOnClickListener(v -> listener.onContributeClick(goal));
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvProgressPercent, tvGoalAmounts, tvSuggestion;
        ProgressBar progressBar;
        Button btnContribute;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tv_goal_name);
            tvProgressPercent = itemView.findViewById(R.id.tv_goal_progress_percent);
            tvGoalAmounts = itemView.findViewById(R.id.tv_goal_amounts);
            tvSuggestion = itemView.findViewById(R.id.tv_suggestion);
            progressBar = itemView.findViewById(R.id.progress_bar_goal);
            btnContribute = itemView.findViewById(R.id.btn_contribute);
        }
    }
}