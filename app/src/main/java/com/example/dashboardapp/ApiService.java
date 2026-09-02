package com.example.dashboardapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("request-otp")
    Call<GenericResponse> requestOTP(@Query("email") String email);

    @POST("verify-otp")
    Call<LoginResponse> verifyOTP(@Query("email") String email, @Query("otp") String otp);

    // This matches: /send-vibe?from_id=...&to_id=...&vibe=...
    @GET("send-vibe")
    Call<VibeResponse> sendVibe(
            @Query("from_id") String fromId,
            @Query("to_id") String toId,
            @Query("vibe") String vibe
    );
}