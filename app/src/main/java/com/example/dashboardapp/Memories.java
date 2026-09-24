package com.example.dashboardapp;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Memories extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiService apiService;
    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memories);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recyclerViewMemories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        myId = prefs.getString("MY_LDR_ID", null);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        loadMemories();
    }

    private void loadMemories() {
        if (myId == null) return;

        apiService.getMemoriesFeed(myId).enqueue(new Callback<List<MemoryItem>>() {
            @Override
            public void onResponse(Call<List<MemoryItem>> call, Response<List<MemoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MemoriesAdapter adapter = new MemoriesAdapter(response.body(), item -> openFullscreenMedia(item));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<MemoryItem>> call, Throwable t) {
                Toast.makeText(Memories.this, "Failed to load memories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Opens full image or streams video with media controls

    private void openFullscreenMedia(MemoryItem item) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_media);

        ImageView fullImage = dialog.findViewById(R.id.fullImageView);
        VideoView fullVideo = dialog.findViewById(R.id.fullVideoView);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseMedia);
        ImageView btnDownload = dialog.findViewById(R.id.btnDownloadMedia);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // DOWNLOAD ACTION
        btnDownload.setOnClickListener(v -> {
            downloadMedia(item.getMediaUrl(), item.getMediaType());
        });

        if ("VIDEO".equalsIgnoreCase(item.getMediaType())) {
            fullVideo.setVisibility(View.VISIBLE);
            fullImage.setVisibility(View.GONE);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(fullVideo);
            fullVideo.setMediaController(mediaController);
            fullVideo.setVideoURI(Uri.parse(item.getMediaUrl()));
            fullVideo.setOnPreparedListener(mp -> fullVideo.start());
        } else {
            fullImage.setVisibility(View.VISIBLE);
            fullVideo.setVisibility(View.GONE);

            Glide.with(this).load(item.getMediaUrl()).into(fullImage);
        }

        dialog.show();
    }

    private void downloadMedia(String url, String mediaType) {
        try {
            boolean isVideo = "VIDEO".equalsIgnoreCase(mediaType);
            String extension = isVideo ? ".mp4" : ".jpg";
            String fileName = "Memory_" + System.currentTimeMillis() + extension;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Saving to Downloads...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Downloading to your Downloads folder...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

   /** private void openFullscreenMedia(MemoryItem item) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_media);

        ImageView fullImage = dialog.findViewById(R.id.fullImageView);
        VideoView fullVideo = dialog.findViewById(R.id.fullVideoView);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseMedia);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        if ("VIDEO".equalsIgnoreCase(item.getMediaType())) {
            fullVideo.setVisibility(View.VISIBLE);
            fullImage.setVisibility(View.GONE);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(fullVideo);
            fullVideo.setMediaController(mediaController);
            fullVideo.setVideoURI(Uri.parse(item.getMediaUrl()));
            fullVideo.setOnPreparedListener(mp -> fullVideo.start());
        } else {
            fullImage.setVisibility(View.VISIBLE);
            fullVideo.setVisibility(View.GONE);

            Glide.with(this).load(item.getMediaUrl()).into(fullImage);
        }

        dialog.show();
    } **/
}