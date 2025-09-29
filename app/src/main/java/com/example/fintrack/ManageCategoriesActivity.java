package com.example.fintrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "ManageCategories";

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList; // This list is now managed by the adapter
    private TextView tvHeader;
    private Button btnAddCategory;

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
        tvHeader = findViewById(R.id.tv_header);
        btnAddCategory = findViewById(R.id.btn_add_category);
    }

    private void initFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        String userId = currentUser.getUid();
        categoriesRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("categories");
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void listenForCategoryUpdates() {
        Log.d(TAG, "Setting up Firestore listener for categories...");
        categoriesRef.orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for category updates", error);
                        Toast.makeText(this, "Error fetching categories.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        List<Category> newCategories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            newCategories.add(doc.toObject(Category.class));
                        }

                        Log.d(TAG, "Firestore data received. Category count: " + newCategories.size());

                        // THIS IS THE CRUCIAL FIX: Use the adapter's method to update the data
                        categoryAdapter.setCategories(newCategories);

                        tvHeader.setVisibility(newCategories.isEmpty() ? View.GONE : View.VISIBLE);
                        rvCategories.setVisibility(newCategories.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        Log.w(TAG, "Firestore listener returned a null value.");
                    }
                });
    }

    @Override
    public void onCategoryClick(Category category) {
        showCategoryDialog(category);
    }

    private void showCategoryDialog(final Category existingCategory) {
        // ... (This method is unchanged)
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