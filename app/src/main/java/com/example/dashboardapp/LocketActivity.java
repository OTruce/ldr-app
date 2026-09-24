package com.example.dashboardapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocketActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_VIDEO_CAPTURE = 103;
    private static final int PERMISSION_CAMERA_REQUEST = 102;

    private Uri currentMediaUri;
    private File currentMediaFile;
    private boolean isRecordingVideo = false;

    private CardView cardFront, cardBackground;
    private ImageView imgLocket;
    private TextView textCaption, textAuthor, textStackCount, textReactionStatus;
    private FloatingActionButton btnCamera;

    private ApiService apiService;
    private String myId;

    // The Stack of Unopened Photos
    private List<LocketPostItem> postStack = new ArrayList<>();
    private int currentIndex = 0;

    private Uri currentPhotoUri;
    private File currentPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locket);

        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        myId = prefs.getString("MY_LDR_ID", null);

        cardFront = findViewById(R.id.cardFront);
        cardBackground = findViewById(R.id.cardBackground);
        imgLocket = findViewById(R.id.imgLocket);
        //textCaption = findViewById(R.id.textCaption);
        textAuthor = findViewById(R.id.textAuthor);
        textStackCount = findViewById(R.id.textStackCount);
        textReactionStatus = findViewById(R.id.textReactionStatus);
        btnCamera = findViewById(R.id.btnCamera);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        //Take to Memories page
        findViewById(R.id.btnHistory).setOnClickListener(
                v -> {
                    Intent intent = new Intent(this, Memories.class);
                    startActivity(intent);
                }
        );

        // 1. Tapping the card flips to the next photo in the stack
        cardFront.setOnClickListener(v -> advanceStack());

        // 2. Setup Camera Shutter
        btnCamera.setOnClickListener(v -> openCamera());

        // 3. Reactions
        setupReactions();

        // 4. Fetch the Stack
        fetchStack();
    }

    private void fetchStack() {
        if (myId == null) return;

        apiService.getLocketStack(myId).enqueue(new Callback<List<LocketPostItem>>() {
            @Override
            public void onResponse(Call<List<LocketPostItem>> call, Response<List<LocketPostItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    postStack = response.body();
                    currentIndex = 0;
                    displayCurrentCard();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<LocketPostItem>> call, Throwable t) {
                showEmptyState();
            }
        });
    }

    private void displayCurrentCard() {
        if (currentIndex >= postStack.size()) {
            showEmptyState();
            return;
        }

        LocketPostItem current = postStack.get(currentIndex);

        // Update Counter
        int remaining = postStack.size() - currentIndex;
        textStackCount.setText(remaining + (remaining == 1 ? " moment" : " moments"));

        // Show secondary background card if more than 1 item is left in the stack
        cardBackground.setVisibility(remaining > 1 ? View.VISIBLE : View.GONE);

        // Load image into rounded card
        Glide.with(this).load(current.getImageUrl()).centerCrop().into(imgLocket);

        //textCaption.setText(current.getCaption() != null && !current.getCaption().isEmpty() ? current.getCaption() : "");
        textAuthor.setText("From " + current.getSenderName());

        if (current.getMyReaction() != null) {
            textReactionStatus.setText("Your reaction: " + current.getMyReaction());
        } else {
            textReactionStatus.setText("Tap an emoji to react");
        }
    }

    private void advanceStack() {
        if (postStack.isEmpty() || currentIndex >= postStack.size()) return;

        LocketPostItem viewedPost = postStack.get(currentIndex);

        // Mark as viewed on the backend so it disappears after this session!
        apiService.markViewed(viewedPost.getId(), myId).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {}
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {}
        });

        currentIndex++;
        displayCurrentCard();
    }

    private void showEmptyState() {
        textStackCount.setText("All caught up!");
        cardBackground.setVisibility(View.GONE);
        imgLocket.setImageDrawable(null);
        //textCaption.setText("No unopened moments right now.");
        textAuthor.setText("");
        textReactionStatus.setText("");
    }

    private void setupReactions() {
        findViewById(R.id.rxHeart).setOnClickListener(v -> sendReaction("❤️"));
        findViewById(R.id.rxHeartEyes).setOnClickListener(v -> sendReaction("😍"));
        findViewById(R.id.rxLaugh).setOnClickListener(v -> sendReaction("😂"));
        findViewById(R.id.rxThumbsUp).setOnClickListener(v -> sendReaction("👍"));
        findViewById(R.id.rxCry).setOnClickListener(v -> sendReaction("😢"));
    }

    private void sendReaction(String emoji) {
        if (postStack.isEmpty() || currentIndex >= postStack.size()) return;

        LocketPostItem current = postStack.get(currentIndex);

        apiService.reactToPost(current.getId(), myId, emoji).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    textReactionStatus.setText("Your reaction: " + emoji);
                    Toast.makeText(LocketActivity.this, "Reacted " + emoji, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
        });
    }

    // --- CAMERA & CAPTION FLOW ---

   /** private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    } *

   private void openCamera() {
       // Check if Camera permission is already granted
       if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
               != android.content.pm.PackageManager.PERMISSION_GRANTED) {

           // Request runtime permission
           androidx.core.app.ActivityCompat.requestPermissions(
                   this,
                   new String[]{android.Manifest.permission.CAMERA},
                   PERMISSION_CAMERA_REQUEST
           );
       } else {
           // Permission already granted -> proceed to launch camera
           launchCameraIntent();
       }
   } **/

   private void openCamera() {
       // Check Camera & Audio permissions (audio is needed for video)
       String[] permissions = {
               android.Manifest.permission.CAMERA,
               android.Manifest.permission.RECORD_AUDIO
       };

       boolean allGranted = true;
       for (String p : permissions) {
           if (androidx.core.content.ContextCompat.checkSelfPermission(this, p)
                   != android.content.pm.PackageManager.PERMISSION_GRANTED) {
               allGranted = false;
               break;
           }
       }

       if (!allGranted) {
           androidx.core.app.ActivityCompat.requestPermissions(this, permissions, PERMISSION_CAMERA_REQUEST);
       } else {
           showMediaChoiceDialog();
       }
   }

    private void showMediaChoiceDialog() {
        String[] options = {"Take High-Res Photo", "Record Video (15s)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Create a Moment")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchPhotoCamera();
                    } else {
                        launchVideoCamera();
                    }
                })
                .show();
    }

    private void launchPhotoCamera() {
        isRecordingVideo = false;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            currentMediaFile = new File(getCacheDir(), "photo_" + System.currentTimeMillis() + ".jpg");
            currentMediaUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", currentMediaFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchVideoCamera() {
        isRecordingVideo = true;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        try {
            currentMediaFile = new File(getCacheDir(), "video_" + System.currentTimeMillis() + ".mp4");
            currentMediaUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", currentMediaFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15); // 15 seconds limit (like Locket)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);    // High quality
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open video camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

/**
    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(this, "Camera could not be opened: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to post moments", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap photoBitmap = (Bitmap) extras.get("data");

            if (photoBitmap != null) {
                promptForCaptionAndUpload(photoBitmap);
            }
        }
    }

    private void promptForCaptionAndUpload(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Caption");

        final EditText input = new EditText(this);
        input.setHint("Write a short message (optional)");
        builder.setView(input);

        builder.setPositiveButton("Post Moment", (dialog, which) -> {
            String caption = input.getText().toString().trim();
            uploadCapturedPhoto(bitmap, caption);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadCapturedPhoto(Bitmap bitmap, String caption) {
        Toast.makeText(this, "Posting moment...", Toast.LENGTH_SHORT).show();

        try {
            // Write bitmap to temporary JPEG file
            File file = new File(getCacheDir(), "temp_locket_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            RequestBody senderBody = RequestBody.create(MediaType.parse("text/plain"), myId);
            RequestBody captionBody = RequestBody.create(MediaType.parse("text/plain"), caption);

            apiService.uploadPhoto(senderBody, captionBody, body).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(LocketActivity.this, "Moment posted to partners!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LocketActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(LocketActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    } **/

private void launchCameraIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        try {
            // 1. Create temporary file for FULL resolution capture
            currentPhotoFile = new File(getCacheDir(), "full_locket_" + System.currentTimeMillis() + ".jpg");
            currentPhotoUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    currentPhotoFile
            );

            // 2. Tell the camera to save the full picture to this URI
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(this, "Could not setup photo storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

  /**  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // The FULL resolution image is saved at currentPhotoFile!
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                promptForCaptionAndUpload(currentPhotoFile);
            }
        }
    } **/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
                if (currentMediaFile != null && currentMediaFile.exists()) {
                    promptForCaptionAndUpload(currentMediaFile, isRecordingVideo);
                }
            }
        }
    }

    private void promptForCaptionAndUpload(File mediaFile, boolean isVideo) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(isVideo ? "Add a Caption to Video" : "Add a Caption to Photo");

        final EditText input = new EditText(this);
        input.setHint("Write a short message (optional)");
        builder.setView(input);

        builder.setPositiveButton("Post Moment", (dialog, which) -> {
            String caption = input.getText().toString().trim();
            uploadMediaFile(mediaFile, caption, isVideo);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadMediaFile(File mediaFile, String caption, boolean isVideo) {
        Toast.makeText(this, isVideo ? "Uploading video moment..." : "Uploading HD photo...", Toast.LENGTH_SHORT).show();

        String mimeType = isVideo ? "video/mp4" : "image/jpeg";
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), mediaFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", mediaFile.getName(), requestFile);
        RequestBody senderBody = RequestBody.create(MediaType.parse("text/plain"), myId);
        RequestBody captionBody = RequestBody.create(MediaType.parse("text/plain"), caption);

        btnCamera.setEnabled(false);

        apiService.uploadPhoto(senderBody, captionBody, body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                btnCamera.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(LocketActivity.this, "Moment posted successfully!", Toast.LENGTH_LONG).show();
                    fetchStack();
                } else {
                    Toast.makeText(LocketActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnCamera.setEnabled(true);
                Toast.makeText(LocketActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptForCaptionAndUpload(File photoFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Caption");

        final EditText input = new EditText(this);
        input.setHint("Write a short message (optional)");
        builder.setView(input);

        builder.setPositiveButton("Post Moment", (dialog, which) -> {
            String caption = input.getText().toString().trim();
            uploadFullQualityPhoto(photoFile, caption);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadFullQualityPhoto(File photoFile, String caption) {
        Toast.makeText(this, "Uploading crystal clear moment...", Toast.LENGTH_SHORT).show();

        // Send the raw full-resolution JPEG file directly
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), photoFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", photoFile.getName(), requestFile);
        RequestBody senderBody = RequestBody.create(MediaType.parse("text/plain"), myId);
        RequestBody captionBody = RequestBody.create(MediaType.parse("text/plain"), caption);

        btnCamera.setEnabled(false);

        apiService.uploadPhoto(senderBody, captionBody, body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                btnCamera.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(LocketActivity.this, "High-definition moment posted!", Toast.LENGTH_LONG).show();
                    fetchStack();
                } else {
                    Toast.makeText(LocketActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnCamera.setEnabled(true);
                Toast.makeText(LocketActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- HISTORY & DELETE DIALOG ---

    private void showHistoryDialog() {
        apiService.getMyHistory(myId).enqueue(new Callback<List<LocketPostItem>>() {
            @Override
            public void onResponse(Call<List<LocketPostItem>> call, Response<List<LocketPostItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<LocketPostItem> myPosts = response.body();
                    String[] titles = new String[myPosts.size()];
                    for (int i = 0; i < myPosts.size(); i++) {
                        String cap = myPosts.get(i).getCaption();
                        titles[i] = (cap != null && !cap.isEmpty() ? cap : "Photo #" + (i + 1));
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(LocketActivity.this);
                    builder.setTitle("Your Past Moments (Tap to delete)");
                    builder.setItems(titles, (dialog, which) -> {
                        LocketPostItem selected = myPosts.get(which);
                        confirmDelete(selected.getId());
                    });
                    builder.setNegativeButton("Close", null);
                    builder.show();
                } else {
                    Toast.makeText(LocketActivity.this, "No past moments found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LocketPostItem>> call, Throwable t) {}
        });
    }

    private void confirmDelete(int postId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Moment")
                .setMessage("Are you sure you want to delete this moment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    apiService.deletePost(postId, myId).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                            Toast.makeText(LocketActivity.this, "Moment deleted", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}