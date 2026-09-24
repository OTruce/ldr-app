package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

public class LoveNoteItem {
    @SerializedName("id")
    private int id;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("ldrid")
    private String senderId;
    @SerializedName("receiver_id")
    private String receiverId;
    @SerializedName("lovenote")
    private String note;

    public int getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getNote() { return note; }
}