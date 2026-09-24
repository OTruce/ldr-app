package com.example.dashboardapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutOtp;
    private TextInputEditText editTextEmail, editTextOTP;
    private MaterialButton buttonLogin, buttonOTP;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOtp = findViewById(R.id.layoutOtp);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextOTP = findViewById(R.id.otpfield);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonOTP = findViewById(R.id.otprequest);

        // INITIALIZE RETROFIT (This is the missing part!)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/") // REPLACE WITH YOUR URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // This line creates the actual "implementation" of your interface
        apiService = retrofit.create(ApiService.class);

        buttonOTP.setOnClickListener(v -> requestOTP());
        buttonLogin.setOnClickListener(v -> attemptLogin());

    }


    private void requestOTP() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Please enter your email");
            return;
        }

        // Show progress to the user
        buttonOTP.setEnabled(false);
        buttonOTP.setText("Sending Code...");

        apiService.requestOTP(email).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    // Success! The code is on its way.
                    Toast.makeText(Login.this, "Check your inbox!", Toast.LENGTH_SHORT).show();

                    // Show the OTP field and the Login button now
                    layoutOtp.setVisibility(View.VISIBLE);
                    buttonLogin.setVisibility(View.VISIBLE);

                    // Change the request button to "Resend" in case they didn't get it
                    buttonOTP.setEnabled(true);
                    buttonOTP.setText("Resend Code");
                } else {
                    buttonOTP.setEnabled(true);
                    buttonOTP.setText("Request OTP");
                    Toast.makeText(Login.this, "Server error. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                buttonOTP.setEnabled(true);
                Toast.makeText(Login.this, "Connection failed. Check Wi-Fi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

   /** private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String otp = editTextOTP.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            layoutOtp.setError("Enter code");
            return;
        }

        // SAFETY CHECK: Make sure the service exists
        if (apiService == null) {
            Toast.makeText(this, "Internal App Error: API not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.verifyOTP(email, otp).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Check if the server actually responded with a 200 OK
                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginData = response.body();

                    // SAFETY CHECK: Make sure the server didn't send null values
                    String myId = loginData.getLdrid() != null ? loginData.getLdrid() : "guest";
                    String myName = loginData.getName() != null ? loginData.getName() : "User";

                    saveUserSession(myId, myName, email);

                    Intent intent = new Intent(Login.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // This handles 401 Unauthorized or 500 Server Error
                    Toast.makeText(Login.this, "Invalid Code or Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // This handles network timeouts
                Toast.makeText(Login.this, "Network Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }**/

   private void attemptLogin() {
       String email = editTextEmail.getText().toString().trim();
       String otp = editTextOTP.getText().toString().trim();

       if (TextUtils.isEmpty(otp)) {
           layoutOtp.setError("Enter code");
           return;
       }

       // Use a Log to see what we are sending
       android.util.Log.d("LDR_DEBUG", "Attempting login for: " + email + " with OTP: " + otp);

       apiService.verifyOTP(email, otp).enqueue(new Callback<LoginResponse>() {
           @Override
           public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
               if (response.isSuccessful() && response.body() != null) {
                   LoginResponse loginData = response.body();

                   // Log what the server sent back
                   android.util.Log.d("LDR_DEBUG", "Server Response: " + loginData.getStatus());

                   String myId = loginData.getLdrid();
                   String myName = loginData.getName();
                   // Inside onResponse of verifyOTP in Login.java:
                   String myGender = response.body().getGender(); // "male" or "female"
                   String myImageUrl = response.body().getImageUrl();


                   // If the server sent back nulls, don't crash, just use defaults
                   if (myId == null) myId = "guest";
                   if (myName == null) myName = "User";
                   getSharedPreferences("LDR_PREFS", MODE_PRIVATE)
                           .edit()
                           .putString("MY_GENDER", myGender)
                           .putString("MY_IMAGE_URL", myImageUrl)
                           .apply();

                   saveUserSession(myId, myName, email,myGender);

                   // Try to move to the next screen
                   try {
                       Intent intent = new Intent(Login.this, DashboardActivity.class);
                       startActivity(intent);
                       finish();
                   } catch (Exception e) {
                       // This will tell you if DashboardActivity is missing from Manifest
                       android.util.Log.e("LDR_DEBUG", "Failed to open Dashboard: " + e.getMessage());
                       Toast.makeText(Login.this, "Check Manifest: " + e.getMessage(), Toast.LENGTH_LONG).show();
                   }

               } else {
                   Toast.makeText(Login.this, "Invalid Code (Server returned " + response.code() + ")", Toast.LENGTH_SHORT).show();
               }
           }

           @Override
           public void onFailure(Call<LoginResponse> call, Throwable t) {
               android.util.Log.e("LDR_DEBUG", "Network Failure: " + t.getMessage());
               Toast.makeText(Login.this, "Network Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
           }
       });
   }


    private void saveUserSession(String ldrid, String name, String email, String gender) {
        getSharedPreferences("LDR_PREFS", MODE_PRIVATE)
                .edit()
                .putString("MY_LDR_ID", ldrid)
                .putString("MY_NAME", name)
                .putString("MY_EMAIL", email)
                .putString("MY_GENDER", gender)
                .putBoolean("IS_LOGGED_IN", true)
                .apply();
    }

}