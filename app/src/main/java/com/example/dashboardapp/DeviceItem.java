package com.example.dashboardapp;

public class DeviceItem {
    private String name;
    private String status;
    private String ldrid;
    private String imageUrl;        // For Profile Picture or Emoji URL
    private String connectionStatus; // "online", "offline", or null (hidden for vibes)
    private String btStatus; // bluetooth activity
    private int iconResId;


    // Constructor for PARTNERS (With Profile Pic and Online Dot)
    public DeviceItem(String name, String status, String ldrid, String imageUrl, String connectionStatus, String btStatus) {
        this.name = name;
        this.status = status;
        this.ldrid = ldrid;
        this.imageUrl = imageUrl;
        this.connectionStatus = connectionStatus;
        this.btStatus = btStatus;
        this.iconResId = R.drawable.ic_wifi;
    }

    // Constructor for VIBES/EMOTES (With Emoji URL, NO status dot)
    public DeviceItem(String name, String status, String ldrid, String imageUrl) {
        this.name = name;
        this.status = status;
        this.ldrid = ldrid;
        this.imageUrl = imageUrl;
        this.connectionStatus = null; // null hides the dot!
        this.btStatus = null;
        this.iconResId = R.drawable.ic_wifi;

    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public int getIconResId() { return iconResId; }
    public String getLdrid() { return ldrid; }
    public String getImageUrl() { return imageUrl; }
    public String getConnectionStatus() { return connectionStatus; }
    public String getBtStatus() { return btStatus; }
}