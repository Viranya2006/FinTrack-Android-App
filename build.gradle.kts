// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // --- Add this line ---
    alias(libs.plugins.google.services) apply false
}