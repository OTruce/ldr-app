package com.example.dashboardapp;

import android.content.SharedPreferences;
import android.os.Bundle;
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

public class BlankActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private ApiService apiService;
    private String myId, targetId, targetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank); // Reusing dashboard layout

        // 1. Get Intent Data (Who are we sending to?)
        targetId = getIntent().getStringExtra("TARGET_LDR_ID");
        targetName = getIntent().getStringExtra("TARGET_NAME");

        // 2. Get My ID
        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        myId = prefs.getString("MY_LDR_ID", null);

        // 3. Setup UI
        TextView header = findViewById(R.id.textViewTitle); // Assuming ID from your layout
        if (header != null) header.setText("Send to " + targetName);

        recyclerView = findViewById(R.id.recyclerViewDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 4. Initialize Networking
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 5. Fetch Vibes & Setup Adapter
        fetchVibes();
    }

    /** private void fetchVibes() {
        apiService.getTextColors().enqueue(new Callback<List<VibeItem>>() {
            @Override
            public void onResponse(Call<List<VibeItem>> call, Response<List<VibeItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeviceItem> displayList = new ArrayList<>();
                    for (VibeItem v : response.body()) {
                        // Map VibeItem to DeviceItem for the existing adapter
                        displayList.add(new DeviceItem(v.getText(), v.getColorName(), R.drawable.ic_wifi, v.getText()));
                    }

                    adapter = new DeviceAdapter(displayList, item -> {
                        // ACTION: Send the vibe when clicked
                        performSendVibe(item.getLdrid()); // We stored vibe text in the ldrid field
                    });
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<VibeItem>> call, Throwable t) {
                Toast.makeText(BlankActivity.this, "Error loading vibes", Toast.LENGTH_SHORT).show();
            }
        });
    } **/

    private void fetchVibes() {
        // 1. Get logged-in user's gender
        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        String myGender = prefs.getString("MY_GENDER", "male");

        apiService.getTextColors().enqueue(new Callback<List<VibeItem>>() {
            @Override
            public void onResponse(Call<List<VibeItem>> call, Response<List<VibeItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeviceItem> displayList = new ArrayList<>();

                    for (VibeItem v : response.body()) {
                        // 2. Select emoji URL based on gender
                        String selectedEmojiUrl;
                        if ("female".equalsIgnoreCase(myGender)) {
                            selectedEmojiUrl = v.getEmojiFemale();
                        } else {
                            selectedEmojiUrl = v.getEmojiMale();
                        }

                        // 3. Status dot is null so it stays hidden for vibes
                        displayList.add(new DeviceItem(
                                v.getText(),
                                v.getColorName(),
                                v.getText(),
                                selectedEmojiUrl
                        ));
                    }

                    adapter = new DeviceAdapter(displayList, item -> {
                        performSendVibe(item.getLdrid());
                    });
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<VibeItem>> call, Throwable t) {}
        });
    }

    private void performSendVibe(String vibeText) {
        apiService.sendVibe(myId, targetId, vibeText).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BlankActivity.this, "Vibe Sent!", Toast.LENGTH_LONG).show();
                    finish(); // Go back to dashboard
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(BlankActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
            }
        });
    }
}