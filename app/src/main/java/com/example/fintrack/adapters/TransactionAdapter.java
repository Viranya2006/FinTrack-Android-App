package com.example.fintrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.models.Transaction;
import com.example.fintrack.utils.CategoryIconUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList; // This is the list the adapter will display
    private final OnTransactionClickListener listener;

    /**
     * Interface to handle clicks on a transaction item.
     */
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    /**
     * Constructor for the adapter.
     * @param context The context from which the adapter is called.
     * @param transactionList The list of transactions to display.
     * @param listener The listener that will handle item clicks.
     */
    public TransactionAdapter(Context context, List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item_layout, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Bind data to the views in the ViewHolder
        holder.tvCategory.setText(transaction.getCategory());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (transaction.getDate() != null) {
            holder.tvDate.setText(sdf.format(transaction.getDate()));
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        String formattedAmount = currencyFormat.format(transaction.getAmount());

        if ("Income".equals(transaction.getType())) {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green_income));
        } else {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red_expense));
        }

        int iconResId = CategoryIconUtil.getIconResourceId(transaction.getCategory());
        holder.ivCategoryIcon.setImageResource(iconResId);
        if ("Income".equals(transaction.getType())) {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green_income));
            holder.ivCategoryIcon.setBackgroundResource(R.drawable.circular_background_green);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.green_income));
        } else {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red_expense));
            holder.ivCategoryIcon.setBackgroundResource(R.drawable.circular_background_red);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.red_expense));
        }

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    /**
     * ViewHolder class for the transaction item layout.
     */
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategory, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}