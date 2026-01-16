package com.example.mealplanner.activities;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mealplanner.R;
import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Profile;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgAvatar;
    private MaterialButton btnEdit, btnSave, btnCancel, btnChangePhoto;
    private TextInputEditText etFullName, etEmail;
    private ProgressBar progress;

    private AuthManager authManager;

    private Profile loadedProfile;
    private Uri selectedAvatarUri;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    imgAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authManager = new AuthManager(this);

        initViews();
        setupListeners();

        loadProfile();
        setEditing(false);
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        progress = findViewById(R.id.progress);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> setEditing(true));

        btnCancel.setOnClickListener(v -> {
            selectedAvatarUri = null;
            fillUiFromProfile(loadedProfile);
            setEditing(false);
        });

        btnChangePhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setEditing(boolean editing) {
        etFullName.setEnabled(editing);

        btnEdit.setVisibility(editing ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnChangePhoto.setVisibility(editing ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEdit.setEnabled(!loading);
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnChangePhoto.setEnabled(!loading);
    }

    private void loadProfile() {
        String token = authManager.getToken();
        if (token == null) return;

        setLoading(true);

        RetrofitClient.getInstance()
                .getApi()
                .getMyProfileTyped("Bearer " + token)
                .enqueue(new ApiCallback<List<Profile>>() {
                    @Override
                    public void onSuccess(List<Profile> response) {
                        setLoading(false);

                        if (response == null || response.isEmpty()) {
                            Toast.makeText(ProfileActivity.this, "Profil nije pronađen.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        loadedProfile = response.get(0);
                        fillUiFromProfile(loadedProfile);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Greška: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fillUiFromProfile(Profile profile) {
        if (profile == null) return;

        etFullName.setText(profile.getFullName() != null ? profile.getFullName() : "");
        etEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");

        String avatarUrl = profile.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void saveProfile() {
        if (loadedProfile == null) return;

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etFullName.setError("Unesite ime i prezime");
            return;
        }

        setLoading(true);

        if (selectedAvatarUri != null) {
            uploadAvatarThenUpdate(fullName, selectedAvatarUri);
        } else {
            updateProfileRow(fullName, loadedProfile.getAvatarUrl());
        }
    }

    private void uploadAvatarThenUpdate(String fullName, Uri avatarUri) {
        try {
            String userId = authManager.getUserId();
            String token = authManager.getToken(); // ✅ USER JWT

            if (userId == null || token == null) {
                setLoading(false);
                Toast.makeText(this, "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = readAllBytes(avatarUri);

            String ext = getFileExtension(avatarUri);
            if (ext == null) ext = "jpg";

            String contentType = getContentType(avatarUri);
            if (contentType == null) contentType = "image/jpeg";

            // ✅ Putanja u bucketu (preporuka): userId/avatar.ext
            String fileName = userId + "/avatar." + ext;

            // ✅ Upload endpoint
            String uploadUrl = Constants.BASE_URL + "storage/v1/object/avatars/" + fileName;

            RequestBody body = RequestBody.create(bytes, MediaType.parse(contentType));

            RetrofitClient.getInstance()
                    .getApi()
                    .uploadAvatar(
                            "Bearer " + token,     // ✅ user token
                            contentType,
                            "true",
                            uploadUrl,
                            body
                    )
                    .enqueue(new ApiCallback<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody response) {
                            // ✅ public url (bucket je public)
                            String publicUrl = Constants.BASE_URL + "storage/v1/object/public/avatars/" + fileName;
                            updateProfileRow(fullName, publicUrl);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            setLoading(false);
                            Toast.makeText(ProfileActivity.this,
                                    "Upload slike nije uspio: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Greška pri čitanju slike: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateProfileRow(String fullName, String avatarUrl) {
        String token = authManager.getToken();
        String userId = authManager.getUserId();

        if (token == null || userId == null) {
            setLoading(false);
            Toast.makeText(this, "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("avatar_url", avatarUrl);

        RetrofitClient.getInstance()
                .getApi()
                .updateProfile("Bearer " + token, "eq." + userId, body)
                .enqueue(new ApiCallback<List<Profile>>() {
                    @Override
                    public void onSuccess(List<Profile> response) {
                        setLoading(false);

                        if (response != null && !response.isEmpty()) {
                            loadedProfile = response.get(0);
                        } else {
                            loadedProfile.setFullName(fullName);
                            loadedProfile.setAvatarUrl(avatarUrl);
                        }

                        selectedAvatarUri = null;
                        fillUiFromProfile(loadedProfile);
                        setEditing(false);

                        Toast.makeText(ProfileActivity.this, "Profil spremljen.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Update nije uspio: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private byte[] readAllBytes(Uri uri) throws Exception {
        InputStream in = getContentResolver().openInputStream(uri);
        if (in == null) throw new Exception("Ne mogu otvoriti sliku.");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        in.close();
        return buffer.toByteArray();
    }

    private String getContentType(Uri uri) {
        ContentResolver cr = getContentResolver();
        return cr.getType(uri);
    }

    private String getFileExtension(Uri uri) {
        String type = getContentType(uri);
        if (type == null) return null;
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
    }
}
