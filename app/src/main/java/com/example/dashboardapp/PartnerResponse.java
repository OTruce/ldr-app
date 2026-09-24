package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

public class PartnerResponse {
    @SerializedName("name") private String name;
    @SerializedName("ldrid") private String ldrid;
    @SerializedName("type") private String type;
    @SerializedName("image_url") private String imageUrl;
    @SerializedName("connection") private String connection;
    @SerializedName("bt_status") private String bluetooth;

    public String getName() { return name; }
    public String getLdrid() { return ldrid; }
    public String getType() { return type; }
    public String getImageUrl() { return imageUrl; }
    public String getConnection() { return connection != null ? connection : "offline"; }
    public String getBluetooth() { return bluetooth != null ? bluetooth : "inactive"; }
}