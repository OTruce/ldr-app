package com.example.dashboardapp;

public class DeviceItem {

    private final String name;
    private final String status;
    private final int iconResId;

    public DeviceItem(String name, String status, int iconResId) {
        this.name = name;
        this.status = status;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getIconResId() {
        return iconResId;
    }
}
