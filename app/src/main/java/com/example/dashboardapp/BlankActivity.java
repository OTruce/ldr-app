package com.example.dashboardapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BlankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        // Kept intentionally blank per spec.
        // The clicked device name is available if needed later:
        // String deviceName = getIntent().getStringExtra(DashboardActivity.EXTRA_DEVICE_NAME);
    }
}
