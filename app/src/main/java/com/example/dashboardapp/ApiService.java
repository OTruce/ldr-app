//package com.example.dashboardapp;
//
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.http.GET;
//import retrofit2.http.POST;
//import retrofit2.http.Query;
//
//public interface ApiService {
//
//    @POST("request-otp")
//    Call<GenericResponse> requestOTP(@Query("email") String email);
//
//    @POST("verify-otp")
//    Call<LoginResponse> verifyOTP(@Query("email") String email, @Query("otp") String otp);
//
//    @GET("get-partners")
//    Call<List<PartnerResponse>> getPartners(@Query("my_id") String myId);
//
//    @GET("get-text-colors")
//    Call<List<VibeItem>> getTextColors();
//
//    @GET("send-vibe")
//    Call<GenericResponse> sendVibe(
//            @Query("from_id") String fromId,
//            @Query("to_id") String toId,
//            @Query("vibe_text") String vibeText
//    );
//}

package com.example.dashboardapp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // --- Existing routes ---
    @POST("request-otp")
    Call<GenericResponse> requestOTP(@Query("email") String email);

    @POST("verify-otp")
    Call<LoginResponse> verifyOTP(@Query("email") String email, @Query("otp") String otp);

    @GET("get-partners")
    Call<List<PartnerResponse>> getPartners(@Query("my_id") String myId);

    @GET("get-text-colors")
    Call<List<VibeItem>> getTextColors();

    @GET("send-vibe")
    Call<GenericResponse> sendVibe(@Query("from_id") String fromId, @Query("to_id") String toId, @Query("vibe_text") String vibeText);

    // --- Feature 1: Love Notes ---
    @POST("love-notes")
    Call<GenericResponse> createLoveNote(
            @Query("sender_id") String senderId,
            @Query("receiver_id") String receiverId,
            @Query("note") String note
    );

    @GET("love-notes/active")
    Call<List<LoveNoteItem>> getActiveLoveNotes(
            @Query("user1_id") String user1Id,
            @Query("user2_id") String user2Id
    );

    // --- Feature 2: Locket ---
    // Post photo (no receiver_id needed!)
    @FormUrlEncoded
    @POST("locket/post")
    Call<GenericResponse> createLocketPost(
            @Field("sender_id") String senderId,
            @Field("image_url") String imageUrl,
            @Field("caption") String caption
    );

    // Get feed of all partners
    @GET("locket/feed")
    Call<List<LocketPostItem>> getLocketFeed(@Query("my_id") String myId);


    // Upload photo using multipart
    @Multipart
    @POST("locket/upload")
    Call<GenericResponse> uploadPhoto(
            @Part("sender_id") RequestBody senderId,
            @Part("caption") RequestBody caption,
            @Part MultipartBody.Part file
    );

    // Get unviewed stack
    @GET("locket/stack")
    Call<List<LocketPostItem>> getLocketStack(@Query("my_id") String myId);

    // Mark as viewed
    @POST("locket/mark-viewed")
    Call<GenericResponse> markViewed(
            @Query("post_id") int postId,
            @Query("viewer_id") String viewerId
    );

    // React
    @POST("locket/react")
    Call<GenericResponse> reactToPost(
            @Query("post_id") int postId,
            @Query("reactor_id") String reactorId,
            @Query("reaction") String reaction
    );

    // History
    @GET("locket/my-history")
    Call<List<LocketPostItem>> getMyHistory(@Query("my_id") String myId);

    // Delete
    @DELETE("locket/delete/{post_id}")
    Call<GenericResponse> deletePost(
            @Path("post_id") int postId,
            @Query("my_id") String myId
    );

    //Memories page
    @GET("memories/feed")
    Call<List<MemoryItem>> getMemoriesFeed(@Query("my_id") String myId);


    @POST("update-fcm-token")
    Call<GenericResponse> updateFcmToken(
            @Query("ldrid") String ldrid,
            @Query("token") String token
    );
}