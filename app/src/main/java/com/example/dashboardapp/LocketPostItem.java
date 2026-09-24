package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

//public class LocketPostItem {
//    @SerializedName("id")
//    private int id;
//    @SerializedName("created_at")
//    private String createdAt;
//    @SerializedName("sender_id")
//    private String senderId;
//    @SerializedName("receiver_id")
//    private String receiverId;
//    @SerializedName("image_url")
//    private String imageUrl;
//    @SerializedName("caption")
//    private String caption;
//    @SerializedName("reaction")
//    private String reaction;
//
//    public int getId() { return id; }
//    public String getCreatedAt() { return createdAt; }
//    public String getSenderId() { return senderId; }
//    public String getReceiverId() { return receiverId; }
//    public String getImageUrl() { return imageUrl; }
//    public String getCaption() { return caption; }
//    public String getReaction() { return reaction; }
//}

public class LocketPostItem {
    @SerializedName("id") private int id;
    @SerializedName("sender_id") private String senderId;
    @SerializedName("sender_name") private String senderName;
    @SerializedName("image_url") private String imageUrl;
    @SerializedName("caption") private String caption;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("my_reaction") private String myReaction;

    public int getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getMyReaction() { return myReaction; }
}