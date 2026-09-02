package com.example.dashboardapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_NAME = "extra_device_name";

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private FrameLayout iconAttachContainer;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerView = findViewById(R.id.recyclerViewDevices);
        iconAttachContainer = findViewById(R.id.iconAttachContainer);

        setupRecyclerView();

        // Paperclip icon is clickable
        iconAttachContainer.setOnClickListener(v ->
                Toast.makeText(this, "Attach clicked", Toast.LENGTH_SHORT).show());

        // Set up the connection to your Brain
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com") // YOUR RENDER URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        boolean isLoggedIn = getSharedPreferences("LDR_PREFS", MODE_PRIVATE)
                .getBoolean("IS_LOGGED_IN", false);

        if (isLoggedIn) {
            // Already logged in? Skip login and go straight to Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            // Not logged in? Show Login screen
            startActivity(new Intent(this, Login.class));
            finish();
        }

    }



    private void setupRecyclerView() {
        List<DeviceItem> devices = new ArrayList<>();
        devices.add(new DeviceItem("Living Room Speaker", "Connected", R.drawable.ic_bluetooth));
        devices.add(new DeviceItem("Home Wifi Router", "Online", R.drawable.ic_wifi));
        devices.add(new DeviceItem("Wireless Headphones", "Disconnected", R.drawable.ic_bluetooth));
        devices.add(new DeviceItem("Office Printer", "Available", R.drawable.ic_wifi));

        adapter = new DeviceAdapter(devices, item -> {
            Intent intent = new Intent(DashboardActivity.this, BlankActivity.class);
            intent.putExtra(EXTRA_DEVICE_NAME, item.getName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
