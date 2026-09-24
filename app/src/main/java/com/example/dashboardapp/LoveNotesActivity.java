package com.example.dashboardapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoveNotesActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompletePartner;
    private TextInputEditText editTextNote;
    private MaterialButton btnSendNote;
    private TextView textPartnerNote, textMyNote, textPartnerNoteHeader;

    private ApiService apiService;
    private String myId;
    private List<PartnerResponse> partnerList = new ArrayList<>();
    private String selectedPartnerId = null;
    private ImageView sender, receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_notes);

        SharedPreferences prefs = getSharedPreferences("LDR_PREFS", MODE_PRIVATE);
        myId = prefs.getString("MY_LDR_ID", null);

        autoCompletePartner = findViewById(R.id.autoCompletePartner);
        editTextNote = findViewById(R.id.editTextNote);
        btnSendNote = findViewById(R.id.btnSendNote);
        textPartnerNote = findViewById(R.id.textPartnerNote);
        textMyNote = findViewById(R.id.textMyNote);
        textPartnerNoteHeader = findViewById(R.id.textPartnerNoteHeader);
        sender = findViewById(R.id.Iconsender);
        receiver = findViewById(R.id.Iconreceiver);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ldr-project.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        loadPartners();

        btnSendNote.setOnClickListener(v -> sendNote());


        //Load YOUR Profile Picture immediately from memory
        String myImageUrl = prefs.getString("MY_IMAGE_URL", null);
        if (myImageUrl != null && !myImageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(myImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_wifi) // Fallback icon
                    .into(sender);
        }
    }

    private void loadPartners() {
        if (myId == null) return;

        apiService.getPartners(myId).enqueue(new Callback<List<PartnerResponse>>() {
            @Override
            public void onResponse(Call<List<PartnerResponse>> call, Response<List<PartnerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    partnerList = response.body();
                    List<String> names = new ArrayList<>();
                    for (PartnerResponse p : partnerList) {
                        names.add(p.getName() + " (" + p.getLdrid() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            LoveNotesActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                    );
                    autoCompletePartner.setAdapter(adapter);

//                    autoCompletePartner.setOnItemClickListener((parent, view, position, id) -> {
//                        selectedPartnerId = partnerList.get(position).getLdrid();
//                        textPartnerNoteHeader.setText("From " + partnerList.get(position).getName() + ":");
//                        loadActiveNotes();
//                    });
//
//                    // Auto-select first partner if available
//                    if (!partnerList.isEmpty()) {
//                        autoCompletePartner.setText(names.get(0), false);
//                        selectedPartnerId = partnerList.get(0).getLdrid();
//                        textPartnerNoteHeader.setText("From " + partnerList.get(0).getName() + ":");

                    // 3. When a partner is tapped from dropdown -> load their photo!
                    autoCompletePartner.setOnItemClickListener((parent, view, position, id) -> {
                        PartnerResponse selectedPartner = partnerList.get(position);
                        selectedPartnerId = selectedPartner.getLdrid();
                        textPartnerNoteHeader.setText("From " + selectedPartner.getName() + ":");

                        // Update Partner Profile Picture
                        loadPartnerImage(selectedPartner.getImageUrl());

                        loadActiveNotes();
                    });

                    // 4. Auto-select first partner and load their photo
                    if (!partnerList.isEmpty()) {
                        PartnerResponse firstPartner = partnerList.get(0);
                        autoCompletePartner.setText(names.get(0), false);
                        selectedPartnerId = firstPartner.getLdrid();
                        textPartnerNoteHeader.setText("From " + firstPartner.getName() + ":");

                        loadPartnerImage(firstPartner.getImageUrl());
                        loadActiveNotes();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PartnerResponse>> call, Throwable t) {
                Toast.makeText(LoveNotesActivity.this, "Failed to load partners", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper to load partner's circular avatar
    private void loadPartnerImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_wifi)
                    .into(receiver);
        } else {
            receiver.setImageResource(R.drawable.ic_wifi);
        }
    }


    /** private void loadActiveNotes() {
        if (selectedPartnerId == null) return;

        apiService.getActiveLoveNotes(myId, selectedPartnerId).enqueue(new Callback<List<LoveNoteItem>>() {
            @Override
            public void onResponse(Call<List<LoveNoteItem>> call, Response<List<LoveNoteItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String myNoteText = "You haven't written a note yet.";
                    String partnerNoteText = "No note received yet.";

                    for (LoveNoteItem item : response.body()) {
                        if (item.getSenderId().equals(myId)) {
                            myNoteText = item.getNote();
                        } else if (item.getSenderId().equals(selectedPartnerId)) {
                            partnerNoteText = item.getNote();
                        }
                    }
                    textMyNote.setText(myNoteText);
                    textPartnerNote.setText(partnerNoteText);
                }
            }

            @Override
            public void onFailure(Call<List<LoveNoteItem>> call, Throwable t) { }
        });
    }

    private void sendNote() {
        String note = editTextNote.getText() != null ? editTextNote.getText().toString().trim() : "";
        if (TextUtils.isEmpty(note)) {
            editTextNote.setError("Note cannot be empty");
            return;
        }
        if (selectedPartnerId == null) {
            Toast.makeText(this, "Select a partner first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendNote.setEnabled(false);

        apiService.createLoveNote(myId, selectedPartnerId, note).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                btnSendNote.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(LoveNotesActivity.this, "Love note saved!", Toast.LENGTH_SHORT).show();
                    editTextNote.setText("");
                    loadActiveNotes();
                } else {
                    Toast.makeText(LoveNotesActivity.this, "Failed to save note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnSendNote.setEnabled(true);
                Toast.makeText(LoveNotesActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    } **/

   private void loadActiveNotes() {
       if (selectedPartnerId == null || myId == null) return;

       apiService.getActiveLoveNotes(myId, selectedPartnerId).enqueue(new Callback<List<LoveNoteItem>>() {
           @Override
           public void onResponse(Call<List<LoveNoteItem>> call, Response<List<LoveNoteItem>> response) {
               if (response.isSuccessful() && response.body() != null) {
                   String myLatestNote = null;
                   String partnerLatestNote = null;

                   // Because the server orders by created_at DESC (newest first),
                   // the first one we encounter is guaranteed to be the latest!
                   for (LoveNoteItem item : response.body()) {
                       if (myLatestNote == null && item.getSenderId().equals(myId)) {
                           myLatestNote = item.getNote();
                       } else if (partnerLatestNote == null && item.getSenderId().equals(selectedPartnerId)) {
                           partnerLatestNote = item.getNote();
                       }
                   }

                   // 1. Update Partner's Note display
                   if (partnerLatestNote != null) {
                       textPartnerNote.setText(partnerLatestNote);
                   } else {
                       textPartnerNote.setText("No active note from partner this week.");
                   }

                   // 2. Update My Note display & Handle the Lock
                   if (myLatestNote != null) {
                       textMyNote.setText(myLatestNote);

                       // LOCK THE SENDING SECTION
                       editTextNote.setEnabled(false);
                       editTextNote.setText(myLatestNote);
                       btnSendNote.setEnabled(false);
                       btnSendNote.setText("Note Active (1 per week)");
                       btnSendNote.setAlpha(0.5f);
                   } else {
                       textMyNote.setText("You haven't written a note yet.");

                       // UNLOCK THE SENDING SECTION
                       editTextNote.setEnabled(true);
                       editTextNote.setText("");
                       btnSendNote.setEnabled(true);
                       btnSendNote.setText("Send Note");
                       btnSendNote.setAlpha(1.0f);
                   }
               }
           }

           @Override
           public void onFailure(Call<List<LoveNoteItem>> call, Throwable t) {
               Toast.makeText(LoveNotesActivity.this, "Failed to sync notes", Toast.LENGTH_SHORT).show();
           }
       });
   }

    private void sendNote() {
        String note = editTextNote.getText() != null ? editTextNote.getText().toString().trim() : "";
        if (TextUtils.isEmpty(note)) {
            editTextNote.setError("Note cannot be empty");
            return;
        }
        if (selectedPartnerId == null) {
            Toast.makeText(this, "Select a partner first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendNote.setEnabled(false);

        apiService.createLoveNote(myId, selectedPartnerId, note).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoveNotesActivity.this, "Love note sent for the week!", Toast.LENGTH_SHORT).show();
                    loadActiveNotes(); // Will fetch and automatically lock the UI
                } else {
                    btnSendNote.setEnabled(true);
                    Toast.makeText(LoveNotesActivity.this, "You already sent a note this week", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnSendNote.setEnabled(true);
                Toast.makeText(LoveNotesActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}