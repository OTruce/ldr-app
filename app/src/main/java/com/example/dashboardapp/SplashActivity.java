package com.example.dashboardapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Duration of the splash screen (3 seconds)
    private static final long SPLASH_DURATION_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this layout exists in your res/layout folder
        setContentView(R.layout.activity_splash);

        // Start a timer. When it finishes, the code inside the {} will run.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // 1. Check if the user is already logged in
            SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);

            Intent intent;
            if (isLoggedIn) {
                // 2a. User is logged in -> Go to Dashboard
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                // 2b. User is NOT logged in -> Go to Login screen
                intent = new Intent(SplashActivity.this, Login.class);
            }

            // 3. Start the next screen
            startActivity(intent);

            // 4. Close the SplashActivity so the user can't "go back" to it
            finish();

        }, SPLASH_DURATION_MS);
    }
}