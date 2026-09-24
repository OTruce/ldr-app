package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MemoryItem {
    @SerializedName("id") private int id;
    @SerializedName("sender_id") private String senderId;
    @SerializedName("sender_name") private String senderName;
    @SerializedName("media_url") private String mediaUrl;
    @SerializedName("media_type") private String mediaType; // "IMAGE" or "VIDEO"
    @SerializedName("caption") private String caption;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("is_owner") private boolean isOwner;
    @SerializedName("reactions") private List<ReactionDetail> reactions;

    public int getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMediaUrl() { return mediaUrl; }
    public String getMediaType() { return mediaType != null ? mediaType : "IMAGE"; }
    public String getCaption() { return caption; }
    public String getCreatedAt() { return createdAt; }
    public boolean isOwner() { return isOwner; }
    public List<ReactionDetail> getReactions() { return reactions; }

    public static class ReactionDetail {
        @SerializedName("reactor_name") private String reactorName;
        @SerializedName("reaction") private String reaction;

        public String getReactorName() { return reactorName; }
        public String getReaction() { return reaction; }
    }
}