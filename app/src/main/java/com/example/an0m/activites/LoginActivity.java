package com.example.an0m.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.an0m.R;
import com.example.an0m.components.ErrorToast;
import com.example.an0m.utils.DeviceInfoProvider;
import com.example.an0m.models.Device;
import com.example.an0m.utils.NavigateUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String androidId;
    private ImageButton loginButton;
    private ProgressBar progressBar;

    private static final String KEY_ALIAS = "an0m_key";


    @SuppressLint({"SetTextI18n", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        TextView androidIdTextView = findViewById(R.id.android_id);
        loginButton = findViewById(R.id.unlock_button);
        progressBar = findViewById(R.id.progress_circle);
        progressBar.setVisibility(View.INVISIBLE);

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        androidIdTextView.setText("ID: " + androidId);
        loginButton.setOnClickListener(v -> onLoginButtonClicked());
    }

    private void onLoginButtonClicked() {
        loginButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        authenticateAnonymously();
    }

    private void authenticateAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                verifyDevice();
            } else {
                Log.e("AnonymousAuth", "Anonymous sign-in failed: ", task.getException());
                denyAccess("Unable to sign in anonymously. Try again later.");
            }
        });
    }

    private void verifyDevice() {

        DocumentReference deviceRef = db
                .collection("approvedDevices")
                .document(androidId);

        deviceRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                denyAccess("Unable to verify the device. Try again later.");
                return;
            }
            DocumentSnapshot deviceDoc = task.getResult();

            if (!deviceDoc.exists()) {
                Device device = buildDeviceProfile(null);
                updateRejected(device,() -> denyAccess("Device is not approved."));
                return;
            }

            if (!isDeviceValid(deviceDoc)) {
                denyAccess("Device is marked as fraudulent.");
                return;
            }

            try {
                KeyPair keyPair = initializeKeyPair();
                PublicKey publicKey = keyPair.getPublic();

                Device device = buildDeviceProfile(publicKey);
                updateApproved(device);

                hasUsername(new UsernameCheckCallback() {
                    public void onResult(boolean usernameFound) {
                        if (usernameFound) {
                            NavigateUtil.navigateToGroupChat(LoginActivity.this);
                        } else {
                            // If username is not found, navigate to AccountSetup
                            NavigateUtil.navigateToAccountSetup(LoginActivity.this);
                        }
                    }
                    public void onError(Exception e) {
                        Log.e("UsernameCheckError", "Error checking username", e);
                        ErrorToast.show(LoginActivity.this, "An error occurred while checking your username. Please try again.");
                    }
                });

            } catch (Exception e) {
                Log.e("KeyPairGeneration", "Error during key generation or upload: " + e.getMessage(), e);
                ErrorToast.show(this, "An error occurred while setting up your account. Please try again.");
            }
        });
    }

    public interface UsernameCheckCallback {
        void onResult(boolean usernameFound);
        void onError(Exception e);
    }

    private void hasUsername(UsernameCheckCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new IllegalStateException("User is not authenticated"));
            return;
        }

        String userId = user.getUid();
        DocumentReference userRef = db.collection("sessions").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError(task.getException());
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (document == null || !document.exists()) {
                callback.onResult(false); // Document doesn't exist
                return;
            }

            String username = document.getString("username");
            if (username == null || username.isEmpty()) {
                callback.onResult(false); // Username is not set
                return;
            }

            callback.onResult(true); // Username is set
        });

    }

    private Device buildDeviceProfile(PublicKey publicKey) {
        Device.DeviceBuilder deviceBuilder = DeviceInfoProvider.getDeviceInfo(this,null);

        deviceBuilder
                .setActive(true)
                .setCompromised(false)
                .setLastLogin(System.currentTimeMillis())
                .setPublicKey(publicKey != null ? publicKeyToString(publicKey) : "");

        return deviceBuilder.build();
    }

    private String publicKeyToString(PublicKey publicKey) {
        if (publicKey == null) {
            return null;
        }
        byte[] encodedPublicKey = publicKey.getEncoded();
        return Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
    }

    private KeyPair initializeKeyPair() {
        try {
            // Get an instance of the KeyStore and load it
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Check if the key pair already exists
            if (keyStore.containsAlias(KEY_ALIAS)) {
                KeyPair keyPair = getKeyPairFromKeystore(keyStore);
                if (keyPair != null) {
                    return keyPair;
                }
            }

            // If key pair does not exist, generate a new one
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder( // properties of the key pair
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1) // ensures process is secure and tamper-resistant
                    .setKeySize(2048)
                    .build());

            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            Log.e("KeyPair", "Error generating key pair: " + e.getMessage());
            throw new RuntimeException("Error occurred on generating Cryptographic Key Pair");
        }
    }

    private KeyPair getKeyPairFromKeystore(KeyStore keyStore) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            if (privateKeyEntry != null) {
                return new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            }
        } catch (Exception e) {
            Log.e("KeyPair", "Error retrieving key pair: " + e.getMessage());
        }
        return null;
    }

    private void updateApproved(Device device) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            ErrorToast.show(this,"User is not authenticated");
            return;
        }
        db.collection("approvedDevices")
                .document(androidId)
                .set(device.toHashMap())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to update device status in approvedDevices", e);
                });
    }

    private void updateRejected(Device device,Runnable onComplete) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            ErrorToast.show(this,"User is not authenticated");
            return;
        }
        db.collection("rejectedDevices")
                .document(androidId)
                .set(device.toHashMap())
                .addOnSuccessListener(task -> {
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to update device status in rejectedDevices", e);
                });
    }

    private boolean isDeviceValid(DocumentSnapshot device) {
        boolean isFraudulent = Boolean.TRUE.equals(device.getBoolean("isFraudulent"));
        return !isFraudulent;
    }

    private void denyAccess(String message) {
        // TODO -> mark fraudulent attempts
        auth.signOut();
        ErrorToast.show(this, message);

        loginButton.setEnabled(false);

        new android.os.Handler().postDelayed(() -> {
            loginButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }, new Random().nextInt(500) + 1000);
    }


}
