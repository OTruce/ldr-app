package com.example.dashboardapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // New channel ID forces Android to apply the new vibration & banner priority
    private static final String CHANNEL_ID = "vibe_channel_v2";

    // Snapchat double-buzz pattern: 0ms wait, 120ms vibrate, 80ms pause, 120ms vibrate
    private static final long[] SNAPCHAT_VIBRATION = new long[]{0, 120, 80, 120};

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String body = null;
        String imageUrl = null;

        // 1. Read from Data payload (Guarantees execution even when app is closed)
        if (!remoteMessage.getData().isEmpty()) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            imageUrl = remoteMessage.getData().get("image");
        }

        // 2. Fallback to notification payload
        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body == null) body = remoteMessage.getNotification().getBody();
            if (imageUrl == null && remoteMessage.getNotification().getImageUrl() != null) {
                imageUrl = remoteMessage.getNotification().getImageUrl().toString();
            }
        }

        if (title != null && body != null) {
            showSnapchatStyleNotification(title, body, imageUrl);
        }
    }

    private void showSnapchatStyleNotification(String title, String body, String imageUrl) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Configure High Priority Channel for Android 8.0+ (Oreo and newer)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Partner Vibes",
                    NotificationManager.IMPORTANCE_HIGH // REQUIRED for floating heads-up banner
            );
            channel.setDescription("Instant partner vibes and signs");
            channel.enableVibration(true);
            channel.setVibrationPattern(SNAPCHAT_VIBRATION);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Tapping the notification opens the Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the Heads-Up Banner Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bluetooth) // Your app icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX) // For Android 7.1 and lower
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setVibrate(SNAPCHAT_VIBRATION);              // Snapchat double-vibrate

        // Attach gender emoji image if present
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Bitmap bitmap = getBitmapFromUrl(imageUrl);
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }
        }

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private Bitmap getBitmapFromUrl(String stringUrl) {
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            return null;
        }
    }
}