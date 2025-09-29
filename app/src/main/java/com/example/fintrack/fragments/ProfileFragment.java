package com.example.fintrack.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fintrack.EditProfileActivity;
import com.example.fintrack.LoginActivity;
import com.example.fintrack.ManageAccountsActivity;
import com.example.fintrack.ManageCategoriesActivity;
import com.example.fintrack.PinLockActivity;
import com.example.fintrack.R;
import com.example.fintrack.SecurityUtils;
import com.example.fintrack.models.Transaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile, btnManageAccounts, btnManageCategories, btnExportCsv, btnLogout;
    private SwitchMaterial securitySwitch;
    private CircleImageView profileAvatar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initFirebase();
        findViews(view);
        setupClickListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (securitySwitch != null) {
            securitySwitch.setChecked(SecurityUtils.isAppLockEnabled(getContext()));
        }
        loadUserProfile();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void findViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        profileAvatar = view.findViewById(R.id.iv_profile_avatar);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnManageAccounts = view.findViewById(R.id.btn_manage_accounts);
        btnManageCategories = view.findViewById(R.id.btn_manage_categories);
        securitySwitch = view.findViewById(R.id.switch_security);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && isAdded()) {
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (isAdded() && documentSnapshot.exists()) {
                    if (tvUserName != null) tvUserName.setText(documentSnapshot.getString("name"));
                    if (tvUserEmail != null) tvUserEmail.setText(documentSnapshot.getString("email"));
                }
            });
        }
    }

    private void setupClickListeners() {
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logoutUser());
        if (btnEditProfile != null) btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        if (btnManageAccounts != null) btnManageAccounts.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageAccountsActivity.class)));
        if (btnManageCategories != null) btnManageCategories.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageCategoriesActivity.class)));
        if (btnExportCsv != null) btnExportCsv.setOnClickListener(v -> checkPermissionsAndExport());

        if (securitySwitch != null) {
            securitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!buttonView.isPressed()) return;
                Intent intent = new Intent(getActivity(), PinLockActivity.class);
                if (SecurityUtils.isAppLockEnabled(getContext())) {
                    intent.putExtra(PinLockActivity.MODE, PinLockActivity.MODE_DISABLE_LOCK);
                } else {
                    intent.putExtra(PinLockActivity.MODE, PinLockActivity.MODE_SET_PIN);
                }
                startActivity(intent);
            });
        }
    }

    // ... (Paste your existing methods for logoutUser, checkPermissionsAndExport, etc. here)
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void checkPermissionsAndExport() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                exportTransactionsToCsv();
            }
        } else {
            exportTransactionsToCsv();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportTransactionsToCsv();
            } else {
                Toast.makeText(getContext(), "Storage permission is required to export data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportTransactionsToCsv() {
        if (mAuth.getCurrentUser() == null) return;
        Toast.makeText(getContext(), "Exporting...", Toast.LENGTH_SHORT).show();
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No transactions to export.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    StringBuilder csvData = new StringBuilder();
                    csvData.append("Date,Type,Category,Amount,Account,Description\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    for (Transaction transaction : queryDocumentSnapshots.toObjects(Transaction.class)) {
                        String description = transaction.getDescription() != null ? transaction.getDescription().replace("\"", "\"\"") : "";
                        csvData.append(sdf.format(transaction.getDate())).append(",");
                        csvData.append(transaction.getType()).append(",");
                        csvData.append(transaction.getCategory()).append(",");
                        csvData.append(transaction.getAmount()).append(",");
                        csvData.append(transaction.getAccountName()).append(",");
                        csvData.append("\"").append(description).append("\"\n");
                    }
                    saveCsvFile(csvData.toString());
                })
                .addOnFailureListener(e -> {
                    if(isAdded()) Toast.makeText(getContext(), "Failed to fetch transactions for export.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCsvFile(String csvData) {
        String fileName = "FinTrack_Export_" + System.currentTimeMillis() + ".csv";
        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContext().getContentResolver().openOutputStream(uri);
            } else {
                java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();
                java.io.File file = new java.io.File(downloadsDir, fileName);
                fos = new java.io.FileOutputStream(file);
            }
            if (fos != null) {
                fos.write(csvData.getBytes());
                fos.close();
                Toast.makeText(getContext(), "Export successful! Saved to Downloads folder.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if(isAdded()) Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}