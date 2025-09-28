package com.example.fintrack;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
    private static final String PREFS_NAME = "FinTrackSecurityPrefs";
    private static final String APP_LOCK_ENABLED_KEY = "app_lock_enabled";
    private static final String PIN_KEY = "app_pin_hash";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setAppLockEnabled(Context context, boolean isEnabled) {
        getPrefs(context).edit().putBoolean(APP_LOCK_ENABLED_KEY, isEnabled).apply();
    }

    public static boolean isAppLockEnabled(Context context) {
        return getPrefs(context).getBoolean(APP_LOCK_ENABLED_KEY, false);
    }

    public static void savePin(Context context, String pin) {
        getPrefs(context).edit().putString(PIN_KEY, hashPin(pin)).apply();
    }

    public static boolean checkPin(Context context, String pin) {
        String savedPinHash = getPrefs(context).getString(PIN_KEY, null);
        return savedPinHash != null && savedPinHash.equals(hashPin(pin));
    }

    public static boolean isPinSet(Context context) {
        return getPrefs(context).getString(PIN_KEY, null) != null;
    }

    private static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}