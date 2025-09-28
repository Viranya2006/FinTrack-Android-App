package com.example.fintrack;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Add this import
import android.content.Intent; // Add this import


import com.example.fintrack.fragments.BudgetsFragment;
import com.example.fintrack.fragments.GoalsFragment;
import com.example.fintrack.fragments.HomeFragment;
import com.example.fintrack.fragments.ProfileFragment;
import com.example.fintrack.fragments.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction; // Add this

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction); // Find the FAB

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.navigation_budgets) {
                selectedFragment = new BudgetsFragment();
            } else if (itemId == R.id.navigation_goals) {
                selectedFragment = new GoalsFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Set the FAB click listener
        fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddTransactionActivity.class));
        });

        // Load the default fragment when the activity is first created
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}