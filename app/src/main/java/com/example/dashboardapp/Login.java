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
                .baseUrl("https://your-app-name.onrender.com/") // REPLACE WITH YOUR URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // This line creates the actual "implementation" of your interface
        apiService = retrofit.create(ApiService.class);

        buttonOTP.setOnClickListener(v -> requestOTP());
        buttonLogin.setOnClickListener(v -> attemptLogin());

    }
//    private void requestOTP(){
//
//    }
//    private void attemptLogin() {
//        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
//        String OTP = editTextOTP.getText() != null ? editTextOTP.getText().toString().trim() : "";
//
//        boolean isValid = true;
//
//        if (TextUtils.isEmpty(email)) {
//            layoutEmail.setError(getString(R.string.error_email_required));
//            isValid = false;
//        } else {
//            layoutEmail.setError(null);
//        }
//
//        if (TextUtils.isEmpty(OTP)) {
//            layoutOtp.setError(getString(R.string.error_otp_required));
//            isValid = false;
//        } else {
//            layoutOtp.setError(null);
//        }
//
//        if (!isValid) {
//            return;
//        }
//
//        // TODO: replace with real authentication logic
//        Intent intent = new Intent(Login.this, DashboardActivity.class);
//        startActivity(intent);
//        finish();
//    }
//}

//    private void requestOTP() {
//        String email = editTextEmail.getText().toString().trim();
//        if (TextUtils.isEmpty(email)) {
//            layoutEmail.setError("Email is required");
//            return;
//        }
//
//        buttonOTP.setEnabled(false); // Prevent double clicking
//        buttonOTP.setText("Sending...");
//
//        apiService.requestOTP(email).enqueue(new Callback<GenericResponse>() {
//            @Override
//            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(Login.this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();
//                    layoutOtp.setVisibility(View.VISIBLE); // Show OTP field
//                } else {
//                    buttonOTP.setEnabled(true);
//                    buttonOTP.setText("Request OTP");
//                    Toast.makeText(Login.this, "Email not found", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GenericResponse> call, Throwable t) {
//                buttonOTP.setEnabled(true);
//                Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

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

//    private void attemptLogin() {
//        String email = editTextEmail.getText().toString().trim();
//        String otp = editTextOTP.getText().toString().trim();
//
//        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(otp)) {
//            return;
//        }
//
//        apiService.verifyOTP(email, otp).enqueue(new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    // SAVE USER DATA LOCALLY
//                    saveUserSession(response.body().getLdrid(), response.body().getName());
//
//                    Intent intent = new Intent(Login.this, DashboardActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    Toast.makeText(Login.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String otp = editTextOTP.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            layoutOtp.setError("Enter the 6-digit code");
            return;
        }

        apiService.verifyOTP(email, otp).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // THE SERVER RETURNED OUR DATA!
                    String myLdrId = response.body().getLdrid();
                    String myName = response.body().getName();

                    // 1. Save this data to the phone's "Shared Preferences"
                    // This makes sure the app remembers who you are even if you close it
                    saveUserSession(myLdrId, myName, email);

                    // 2. Go to the Dashboard
                    Intent intent = new Intent(Login.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Incorrect code. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Login.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Very important: The app must "remember" who is logged in
//    private void saveUserSession(String ldrid, String name, String email) {
//        getSharedPreferences("LDR_PREFS", MODE_PRIVATE)
//                .edit()
//                .putString("my_id", ldrid)
//                .putString("my_name", name)
//                .apply();

    private void saveUserSession(String ldrid, String name, String email) {
        getSharedPreferences("LDR_PREFS", MODE_PRIVATE)
                .edit()
                .putString("MY_LDR_ID", ldrid)
                .putString("MY_NAME", name)
                .putString("MY_EMAIL", email)
                .putBoolean("IS_LOGGED_IN", true)
                .apply();
    }

}