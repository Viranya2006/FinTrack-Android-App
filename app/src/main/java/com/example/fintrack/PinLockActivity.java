package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class PinLockActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String MODE = "MODE";
    public static final int MODE_SET_PIN = 0;
    public static final int MODE_CONFIRM_PIN = 1;
    public static final int MODE_AUTHENTICATE = 2;
    public static final int MODE_DISABLE_LOCK = 3;

    private int currentMode;
    private StringBuilder pinBuilder = new StringBuilder();
    private String firstPin = null;

    private TextView tvPrompt;
    private LinearLayout dotsLayout;
    private List<ImageView> dots;
    private ImageView btnFingerprint;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        currentMode = getIntent().getIntExtra(MODE, MODE_AUTHENTICATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvPrompt = findViewById(R.id.tv_prompt);
        dotsLayout = findViewById(R.id.dots_layout);
        btnFingerprint = findViewById(R.id.btn_fingerprint);

        setupPinPadListeners();
        initializeDots();
        updateUiForMode();
    }

    private void initializeDots() {
        dots = new ArrayList<>();
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            dots.add((ImageView) dotsLayout.getChildAt(i));
        }
    }

    private void setupPinPadListeners() {
        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
        findViewById(R.id.btn_5).setOnClickListener(this);
        findViewById(R.id.btn_6).setOnClickListener(this);
        findViewById(R.id.btn_7).setOnClickListener(this);
        findViewById(R.id.btn_8).setOnClickListener(this);
        findViewById(R.id.btn_9).setOnClickListener(this);
        findViewById(R.id.btn_0).setOnClickListener(this);
        findViewById(R.id.btn_backspace).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        btnFingerprint.setOnClickListener(this);
    }

    private void updateUiForMode() {
        switch (currentMode) {
            case MODE_SET_PIN:
                getSupportActionBar().setTitle("Set App Lock");
                tvPrompt.setText("Create a 6-digit PIN");
                break;
            case MODE_CONFIRM_PIN:
                getSupportActionBar().setTitle("Confirm PIN");
                tvPrompt.setText("Re-enter your PIN");
                break;
            case MODE_AUTHENTICATE:
            case MODE_DISABLE_LOCK:
                getSupportActionBar().setTitle("Unlock FinTrack");
                tvPrompt.setText("Enter your PIN");
                setupBiometrics();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_backspace) {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
            }
        } else if (v.getId() == R.id.btn_confirm) {
            handleConfirm();
        } else if (v.getId() == R.id.btn_fingerprint) {
            if (canUseBiometrics()) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(this, "Biometric authentication not available.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (pinBuilder.length() < 6) {
                pinBuilder.append(((TextView) v).getText());
            }
        }
        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < dots.size(); i++) {
            if (i < pinBuilder.length()) {
                dots.get(i).setImageResource(R.drawable.dot_filled);
            } else {
                dots.get(i).setImageResource(R.drawable.dot_empty);
            }
        }
    }

    private void handleConfirm() {
        if (pinBuilder.length() != 6) {
            Toast.makeText(this, "PIN must be 6 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (currentMode) {
            case MODE_SET_PIN:
                firstPin = pinBuilder.toString();
                currentMode = MODE_CONFIRM_PIN;
                updateUiForMode();
                resetPin();
                break;
            case MODE_CONFIRM_PIN:
                if (pinBuilder.toString().equals(firstPin)) {
                    SecurityUtils.savePin(this, firstPin);
                    SecurityUtils.setAppLockEnabled(this, true);
                    Toast.makeText(this, "App Lock Enabled", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show();
                    currentMode = MODE_SET_PIN;
                    updateUiForMode();
                    resetPin();
                }
                break;
            case MODE_AUTHENTICATE:
                if (SecurityUtils.checkPin(this, pinBuilder.toString())) {
                    unlockApp();
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    resetPin();
                }
                break;
            case MODE_DISABLE_LOCK:
                if (SecurityUtils.checkPin(this, pinBuilder.toString())) {
                    SecurityUtils.setAppLockEnabled(this, false);
                    Toast.makeText(this, "App Lock Disabled", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    resetPin();
                }
                break;
        }
    }

    private void resetPin() {
        pinBuilder.setLength(0);
        updateDots();
    }

    private void unlockApp() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- Biometrics Logic ---
    private boolean canUseBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void setupBiometrics() {
        if (!canUseBiometrics()) {
            btnFingerprint.setVisibility(View.GONE);
            return;
        }
        btnFingerprint.setVisibility(View.VISIBLE);

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (currentMode == MODE_AUTHENTICATE) {
                    unlockApp();
                } else if (currentMode == MODE_DISABLE_LOCK) {
                    SecurityUtils.setAppLockEnabled(PinLockActivity.this, false);
                    Toast.makeText(PinLockActivity.this, "App Lock Disabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("FinTrack Authentication")
                .setSubtitle("Use your fingerprint or face to unlock")
                .setNegativeButtonText("Use PIN")
                .build();

        // Automatically trigger prompt on load for authentication mode
        if (currentMode == MODE_AUTHENTICATE) {
            // A small delay allows the activity transition to finish smoothly
            new Handler(Looper.getMainLooper()).postDelayed(() -> biometricPrompt.authenticate(promptInfo), 300);
        }
    }
}