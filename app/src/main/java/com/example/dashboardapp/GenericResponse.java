package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("detail")
    private String detail; // In case of errors

    public String getStatus() { return status; }
    public String getDetail() { return detail; }
}
