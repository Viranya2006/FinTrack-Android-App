package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.fintrack.PinLockActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // In SplashActivity.java
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is signed in, check if app lock is enabled
                if (SecurityUtils.isAppLockEnabled(this)) {
                    Intent intent = new Intent(SplashActivity.this, PinLockActivity.class);
                    intent.putExtra("MODE", PinLockActivity.MODE_AUTHENTICATE);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                }
            } else {
                // No user is signed in
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500);
    }
}