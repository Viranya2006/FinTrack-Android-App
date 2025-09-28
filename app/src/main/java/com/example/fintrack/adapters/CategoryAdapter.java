package com.example.fintrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.models.Category;
import com.example.fintrack.utils.CategoryIconUtil;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon, ivEdit;
        TextView tvCategoryName, tvCategoryType;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryType = itemView.findViewById(R.id.tv_category_type);
            ivEdit = itemView.findViewById(R.id.iv_edit_category);
        }

        public void bind(final Category category, final OnCategoryClickListener listener) {
            tvCategoryName.setText(category.getName());
            tvCategoryType.setText(category.getType());
            ivCategoryIcon.setImageResource(CategoryIconUtil.getIconResourceId(category.getName()));
            itemView.setOnClickListener(v -> listener.onCategoryClick(category));
            ivEdit.setOnClickListener(v -> listener.onCategoryClick(category));
        }
    }
}