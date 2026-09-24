package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status") private String status;
    @SerializedName("ldrid") private String ldrid;
    @SerializedName("name") private String name;
    @SerializedName("gender") private String gender;
    @SerializedName("image_url") private String imageUrl;

    public String getStatus() { return status; }
    public String getLdrid() { return ldrid; }
    public String getName() { return name; }
    public String getGender() { return gender != null ? gender : "male"; }
    public String getImageUrl() { return imageUrl; }
}