package com.example.fintrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.adapters.CategoryAdapter;
import com.example.fintrack.models.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    private Button btnAddCategory;
    private TextView tvHeader;

    private CollectionReference categoriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        setupToolbar();
        initViews();
        initFirebase();
        setupRecyclerView();
        listenForCategoryUpdates();

        btnAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rv_categories);
        btnAddCategory = findViewById(R.id.btn_add_category);
        tvHeader = findViewById(R.id.tv_header);
    }

    private void initFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String userId = currentUser.getUid();
        categoriesRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("categories");
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(categoryList, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void listenForCategoryUpdates() {
        categoriesRef.orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error fetching categories.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        categoryList.clear();
                        categoryList.addAll(value.toObjects(Category.class));
                        categoryAdapter.notifyDataSetChanged();
                        tvHeader.setVisibility(categoryList.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    @Override
    public void onCategoryClick(Category category) {
        showCategoryDialog(category);
    }

    private void showCategoryDialog(final Category existingCategory) {
        // ... (This method is the same as before, handling add/edit/delete)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        final TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        final EditText etCategoryName = dialogView.findViewById(R.id.et_category_name);
        final EditText etCategoryType = dialogView.findViewById(R.id.et_category_type);
        builder.setView(dialogView);
        if (existingCategory != null) {
            tvTitle.setText("Edit Category");
            etCategoryName.setText(existingCategory.getName());
            etCategoryType.setText(existingCategory.getType());
            etCategoryName.setEnabled(false);
            builder.setPositiveButton("Save", (dialog, which) -> {
                String type = etCategoryType.getText().toString().trim();
                if (TextUtils.isEmpty(type)) return;
                categoriesRef.document(existingCategory.getName()).update("type", type);
            });
            builder.setNegativeButton("Delete", (dialog, which) -> confirmDelete(existingCategory));
            builder.setNeutralButton("Cancel", null);
        } else {
            tvTitle.setText("Add New Category");
            builder.setPositiveButton("Add", (dialog, which) -> {
                String name = etCategoryName.getText().toString().trim();
                String type = etCategoryType.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(type)) return;
                String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                Category newCategory = new Category(formattedName, type);
                categoriesRef.document(formattedName).set(newCategory);
            });
            builder.setNegativeButton("Cancel", null);
        }
        builder.create().show();
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (d, w) -> categoriesRef.document(category.getName()).delete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}