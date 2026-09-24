package com.example.dashboardapp;

import com.google.gson.annotations.SerializedName;

public class VibeItem {
    @SerializedName("text") private String text;
    @SerializedName("color") private String colorName;
    @SerializedName("color_code") private String hexCode;
    @SerializedName("emoji_male") private String emojiMale;
    @SerializedName("emoji_female") private String emojiFemale;

    public String getText() { return text; }
    public String getColorName() { return colorName; }
    public String getHexCode() { return hexCode; }
    public String getEmojiMale() { return emojiMale; }
    public String getEmojiFemale() { return emojiFemale; }
}