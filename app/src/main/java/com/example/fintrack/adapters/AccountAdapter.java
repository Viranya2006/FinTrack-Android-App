package com.example.fintrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.models.Account;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final Context context;
    private final List<Account> accountList;
    private OnAccountClickListener listener; // Add this

    // Add this interface
    public interface OnAccountClickListener {
        void onEditClick(Account account);
        void onDeleteClick(Account account);
    }

    // Modify constructor
    public AccountAdapter(Context context, List<Account> accountList, OnAccountClickListener listener) {
        this.context = context;
        this.accountList = accountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.account_item_layout, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvAccountName.setText(account.getName());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        holder.tvAccountBalance.setText(format.format(account.getBalance()));

        // Make icons functional
        holder.ivEditAccount.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(account);
        });
        holder.ivDeleteAccount.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(account);
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName, tvAccountBalance;
        ImageView ivEditAccount, ivDeleteAccount;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tv_account_name);
            tvAccountBalance = itemView.findViewById(R.id.tv_account_balance);
            ivEditAccount = itemView.findViewById(R.id.iv_edit_account);
            ivDeleteAccount = itemView.findViewById(R.id.iv_delete_account);
        }
    }
}