package com.example.an0m.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.an0m.R;
import com.example.an0m.utils.NavigateUtil;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountSetupActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView usernamePreview;
    private EditText usernameInput;
    private AppCompatButton continueButton;
    private LinearProgressIndicator linearProgressIndicator;

    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        usernamePreview = findViewById(R.id.username_preview);
        usernameInput = findViewById(R.id.username_input);
        continueButton = findViewById(R.id.continue_button);
        linearProgressIndicator = findViewById(R.id.linear_progress);
        linearProgressIndicator.setVisibility(View.INVISIBLE);

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }
                @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                usernamePreview.setText(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        continueButton.setOnClickListener(v -> onContinueButtonClicked());
    }

    private void onContinueButtonClicked() {
        continueButton.setEnabled(false);
        linearProgressIndicator.setVisibility(View.VISIBLE);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {

            final String authId = user.getUid();
            final String username = usernameInput.getText().toString();
            @SuppressLint("HardwareIds") final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            Map<String,String> session = new HashMap<>();
            session.put("username",username);
            session.put("device",androidId);

            db.collection("sessions").document(authId).set(session)
                    .addOnFailureListener(e -> {
                        throw new RuntimeException(e);
                    });

            NavigateUtil.navigateToGroupChat(this);
        } else {
            throw new IllegalStateException("User is not authenticated");
        }
    }

}
