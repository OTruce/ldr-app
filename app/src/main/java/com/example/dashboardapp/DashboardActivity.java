package com.example.dashboardapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.firebase.messaging.FirebaseMessaging;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private ApiService apiService;
    private String myLdrId;
    ImageButton locket, affirm;
    private ImageView btnRefresh;
    private Handler statusPollHandler = new Handler(Looper.getMainLooper());
    private Runnable statusPollRunnable;
    private static final int POLL_INTERVAL_MS = 5000; // Check every 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        checkNotificationPermissionAndSyncToken();
        syncNotificationToken();

        // 1. Setup UI
        recyclerView = findViewById(R.id.recyclerViewDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize with empty list
        adapter = new DeviceAdapter(new ArrayList<>(), item -> {
            // ACTION: When a partner is clicked, go to the "Send Vibe" screen
            Intent intent = new Intent(DashboardActivity.this, BlankActivity.class);
            intent.putExtra("TARGET_LDR_ID", item.getLdrid());
            intent.putExtra("TARGET_NAME", item.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // 2. Get my ID from Login
        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        myLdrId = prefs.getString("MY_LDR_ID", null);

        // 3. Setup Networking
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 4. Fetch the data!
        fetchPartners();

        // To locket
        locket = findViewById(R.id.locket);
        locket.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LocketActivity.class);
            startActivity(intent);
        });

        // To weekly lovenotes
        affirm = findViewById(R.id.affirm);
        affirm.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LoveNotesActivity.class);
            startActivity(intent);
        });

        //Refresh Feature
        btnRefresh = findViewById(R.id.btnRefresh);
        if( btnRefresh != null) {
            btnRefresh.setOnClickListener(
                    v -> handleManualRefresh()
            );
        }

        //TextView textDeviceCount = findViewById(R.id.textDeviceCount);
       // textDeviceCount.setText(String.valueOf(partnersForDisplay.size()));

        // Define the repeating task
        statusPollRunnable = new Runnable() {
            @Override
            public void run() {
                // Silently refresh the list in the background
                fetchPartnersSilently();
                // Schedule next poll in 5 seconds
                statusPollHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };

    }

    private void checkNotificationPermissionAndSyncToken() {
        // 1. Request runtime permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }

        // 2. Ask Google for this phone's unique FCM Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                android.util.Log.d("FCM_DEBUG", "My Phone's FCM Token: " + token);

                // 3. Send the token to your Python server to save it in Supabase
                if (myLdrId != null) {
                    apiService.updateFcmToken(myLdrId, token).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                            if (response.isSuccessful()) {
                                android.util.Log.d("FCM_DEBUG", "Token successfully saved in Supabase!");
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            android.util.Log.e("FCM_DEBUG", "Failed to save token: " + t.getMessage());
                        }
                    });
                }
            }
        });
    }

    // Call this inside onCreate() of DashboardActivity:
    private void syncNotificationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();

                // Send token to your Python server
                apiService.updateFcmToken(myLdrId, token).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {}
                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {}
                });
            }
        });
    }

    private void fetchPartners() {
        if (myLdrId == null) return;

        apiService.getPartners(myLdrId).enqueue(new Callback<List<PartnerResponse>>() {
            @Override
            public void onResponse(Call<List<PartnerResponse>> call, Response<List<PartnerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeviceItem> partnersForDisplay = new ArrayList<>();

                    for (PartnerResponse p : response.body()) {
                        partnersForDisplay.add(new DeviceItem(
                                p.getName(),
                                p.getType(), // e.g. "Romantic"
                                p.getLdrid(),
                                p.getImageUrl(),     // Profile Picture URL
                                p.getConnection(),   // "online" or "offline"
                                p.getBluetooth() // Bluetooth or user proximity

                        ));
                    }

                    // Push the real database users into the list
                    adapter.updateData(partnersForDisplay);
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load partners, Refresh the page to try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PartnerResponse>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleManualRefresh() {
        // 1. Visually spin the refresh icon while connecting
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(700);
        rotate.setRepeatCount(Animation.INFINITE);
        btnRefresh.startAnimation(rotate);

        Toast.makeText(this, "Calling server...", Toast.LENGTH_SHORT).show();

        // 2. Re-attempt to load partners and verify server health
        apiService.getPartners(myLdrId).enqueue(new Callback<List<PartnerResponse>>() {
            @Override
            public void onResponse(Call<List<PartnerResponse>> call, Response<List<PartnerResponse>> response) {
                // Stop the spinning animation
                btnRefresh.clearAnimation();

                if (response.isSuccessful() && response.body() != null) {
                    List<DeviceItem> partnersForDisplay = new ArrayList<>();
                    for (PartnerResponse p : response.body()) {
                        partnersForDisplay.add(new DeviceItem(
                                p.getName(),
                                p.getType(),
                                p.getLdrid(),
                                p.getImageUrl(),     // Profile Picture URL
                                p.getConnection(),    // "online" or "offline"
                                p.getBluetooth() // Bluetooth or user proximity
                        ));
                    }
                    adapter.updateData(partnersForDisplay);
                    Toast.makeText(DashboardActivity.this, "Success! Server is awake.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Server waking up, try again in 10s...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PartnerResponse>> call, Throwable t) {
                // Stop the spinning animation
                btnRefresh.clearAnimation();
                Toast.makeText(DashboardActivity.this, "Still waking up (Cold start)... please wait a few seconds and tap again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start polling when the user is looking at the screen
        statusPollHandler.post(statusPollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // STOP polling immediately when the user leaves the screen (saves battery & data!)
        statusPollHandler.removeCallbacks(statusPollRunnable);
    }

    // Silent version of fetchPartners that doesn't show error toasts while polling
    private void fetchPartnersSilently() {
        if (myLdrId == null) return;

        apiService.getPartners(myLdrId).enqueue(new Callback<List<PartnerResponse>>() {
            @Override
            public void onResponse(Call<List<PartnerResponse>> call, Response<List<PartnerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeviceItem> updatedList = new ArrayList<>();
                    for (PartnerResponse p : response.body()) {
                        updatedList.add(new DeviceItem(
                                p.getName(),
                                p.getType(),
                                p.getLdrid(),
                                p.getImageUrl(),
                                p.getConnection(), // "online" or "offline"
                                p.getBluetooth() // Bluetooth or user proximity
                        ));
                    }
                    // This updates the status dot color dynamically
                    adapter.updateData(updatedList);
                }
            }

            @Override
            public void onFailure(Call<List<PartnerResponse>> call, Throwable t) {
                // Silently ignore temporary blips while polling
            }
        });
    }
}