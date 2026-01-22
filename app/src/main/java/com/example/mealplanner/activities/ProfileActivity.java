package com.example.mealplanner.activities;

import android.content.ContentResolver;
import android.content.Intent;
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
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.Profile;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    private MaterialButton btnEdit, btnSave, btnCancel, btnChangePhoto, btnDeleteAccount;
    private MaterialButton btnLogout;

    private TextInputEditText etFullName, etEmail;
    private ProgressBar progress;

    private AuthManager authManager;

    private Profile loadedProfile;
    private Uri selectedAvatarUri;

    private String originalFullName = "";
    private String originalEmail = "";

    private BottomNavigationView bottomNav;

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
        setupBottomNav();
        setupListeners();

        loadProfile();
        setEditing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_profile);
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);

        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);


        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);

        progress = findViewById(R.id.progress);
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            Intent i = null;

            if (id == R.id.nav_home) i = new Intent(this, MainActivity.class);
            else if (id == R.id.nav_recipes) i = new Intent(this, RecipesListActivity.class);
            else if (id == R.id.nav_planner) i = new Intent(this, MyMealPlansActivity.class);
            else if (id == R.id.nav_shopping) i = new Intent(this, ShoppingListsActivity.class);

            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            originalFullName = etFullName.getText() != null ? etFullName.getText().toString() : "";
            originalEmail = etEmail.getText() != null ? etEmail.getText().toString() : "";

            setEditing(true);
            etFullName.requestFocus();
        });

        btnCancel.setOnClickListener(v -> {
            selectedAvatarUri = null;

            etFullName.setText(originalFullName);
            etEmail.setText(originalEmail);

            fillUiFromProfile(loadedProfile);
            setEditing(false);
        });

        btnChangePhoto.setOnClickListener(v -> {
            String[] options = {"Choose from gallery", "Choose avatar"};

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Change photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            pickImage.launch("image/*");
                        } else {
                            showAvatarPicker();
                        }
                    })
                    .show();
        });

        btnSave.setOnClickListener(v -> saveProfile());

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(ProfileActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                authManager.logout();

                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        btnDeleteAccount.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete account")
                    .setMessage("This will permanently delete your account and all data. Continue?")
                    .setPositiveButton("Delete", (d, w) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setEditing(boolean editing) {
        etFullName.setEnabled(editing);
        etFullName.setFocusable(editing);
        etFullName.setFocusableInTouchMode(editing);

        etEmail.setEnabled(editing);
        etEmail.setFocusable(editing);
        etEmail.setFocusableInTouchMode(editing);

        btnEdit.setVisibility(editing ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnChangePhoto.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnDeleteAccount.setVisibility(editing ? View.VISIBLE : View.GONE);

        if (btnLogout != null) btnLogout.setVisibility(editing ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);

        btnEdit.setEnabled(!loading);
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnChangePhoto.setEnabled(!loading);
        if (btnLogout != null) btnLogout.setEnabled(!loading);
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
                            Toast.makeText(ProfileActivity.this, "Profile not found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        loadedProfile = response.get(0);
                        fillUiFromProfile(loadedProfile);

                        originalFullName = etFullName.getText() != null ? etFullName.getText().toString() : "";
                        originalEmail = etEmail.getText() != null ? etEmail.getText().toString() : "";
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fillUiFromProfile(Profile profile) {
        if (profile == null) return;

        String fullName = profile.getFullName() != null ? profile.getFullName() : "";
        String email = profile.getEmail() != null ? profile.getEmail() : "";

        etFullName.setText(fullName);
        etEmail.setText(email);

        String avatarUrl = profile.getAvatarUrl();
        if (avatarUrl != null && avatarUrl.startsWith("http")) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(imgAvatar);
        } else {
            Integer resId = getAvatarResId(avatarUrl);
            if (resId != null) {
                imgAvatar.setImageResource(resId);
            } else {
                imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        }

        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("full_name", fullName)
                .apply();
    }

    private void saveProfile() {
        if (loadedProfile == null) return;

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        // NE DIRAM setError tekstove
        if (fullName.isEmpty()) { etFullName.setError("Unesite ime i prezime"); return; }
        if (email.isEmpty()) { etEmail.setError("Unesite email"); return; }

        setLoading(true);

        if (selectedAvatarUri != null) {
            uploadAvatarThenUpdate(fullName, email, selectedAvatarUri);
        } else {
            updateProfileRow(fullName, email, loadedProfile.getAvatarUrl());
        }
    }

    private void uploadAvatarThenUpdate(String fullName, String email, Uri avatarUri) {
        try {
            String userId = authManager.getUserId();
            String token = authManager.getToken();

            if (userId == null || token == null) {
                setLoading(false);
                Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = readAllBytes(avatarUri);

            String ext = getFileExtension(avatarUri);
            if (ext == null) ext = "jpg";

            String contentType = getContentType(avatarUri);
            if (contentType == null) contentType = "image/jpeg";

            String fileName = userId + "/avatar_" + System.currentTimeMillis() + "." + ext;

            String uploadUrl = Constants.BASE_URL + "storage/v1/object/avatars/" + fileName;
            RequestBody body = RequestBody.create(bytes, MediaType.parse(contentType));

            RetrofitClient.getInstance()
                    .getApi()
                    .uploadAvatar(
                            "Bearer " + token,
                            contentType,
                            "true",
                            uploadUrl,
                            body
                    )
                    .enqueue(new ApiCallback<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody response) {
                            String publicUrl = Constants.BASE_URL + "storage/v1/object/public/avatars/" + fileName;
                            updateProfileRow(fullName, email, publicUrl);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            setLoading(false);
                            Toast.makeText(ProfileActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateProfileRow(String fullName, String email, String avatarUrl) {
        String token = authManager.getToken();
        String userId = authManager.getUserId();

        if (token == null || userId == null) {
            setLoading(false);
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("avatar_url", avatarUrl);

        RetrofitClient.getInstance()
                .getApi()
                .updateProfile("Bearer " + token, "eq." + userId, body)
                .enqueue(new ApiCallback<List<Profile>>() {
                    @Override
                    public void onSuccess(List<Profile> response) {
                        setLoading(false);

                        if (loadedProfile != null) {
                            loadedProfile.setFullName(fullName);
                            loadedProfile.setEmail(email);
                            loadedProfile.setAvatarUrl(avatarUrl);
                        }

                        if (response != null && !response.isEmpty()) loadedProfile = response.get(0);

                        selectedAvatarUri = null;
                        fillUiFromProfile(loadedProfile);

                        originalFullName = fullName;
                        originalEmail = email;

                        setEditing(false);
                        Toast.makeText(ProfileActivity.this, "Profile saved.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private byte[] readAllBytes(Uri uri) throws Exception {
        InputStream in = getContentResolver().openInputStream(uri);
        if (in == null) throw new Exception("Cannot open image.");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) buffer.write(data, 0, nRead);
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

    private Integer getAvatarResId(String avatarKey) {
        if (avatarKey == null) return null;

        switch (avatarKey) {
            case "avatar_female1": return R.drawable.avatar_female1;
            case "avatar_female2": return R.drawable.avatar_female2;
            case "avatar_male1": return R.drawable.avatar_male1;
            case "avatar_male2": return R.drawable.avatar_male2;
            default: return null;
        }
    }

    private void showAvatarPicker() {

        View view = getLayoutInflater().inflate(R.layout.dialog_avatar_picker, null);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Choose avatar")
                        .setView(view)
                        .create();

        view.findViewById(R.id.avatarFemale1).setOnClickListener(v -> selectAvatar("avatar_female1", dialog));
        view.findViewById(R.id.avatarFemale2).setOnClickListener(v -> selectAvatar("avatar_female2", dialog));
        view.findViewById(R.id.avatarMale1).setOnClickListener(v -> selectAvatar("avatar_male1", dialog));
        view.findViewById(R.id.avatarMale2).setOnClickListener(v -> selectAvatar("avatar_male2", dialog));

        dialog.show();
    }

    private void selectAvatar(String avatarKey, androidx.appcompat.app.AlertDialog dialog) {
        selectedAvatarUri = null;
        imgAvatar.setImageResource(getAvatarResId(avatarKey));

        if (loadedProfile != null) {
            loadedProfile.setAvatarUrl(avatarKey);
        }

        dialog.dismiss();
    }

    private void deleteAccount() {

        String token = authManager.getToken();
        String userId = authManager.getUserId();

        if (token == null || userId == null) return;

        SupabaseAPI api = RetrofitClient.getInstance().getApi();

        Map<String, String> body = new HashMap<>();
        body.put("user_id", userId);

        api.deleteAuthUser("Bearer " + token, body).enqueue(new ApiCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody r) {

                String auth = "Bearer " + token;
                String eq = "eq." + userId;

                api.deleteShoppingItemsByUser(auth, eq).enqueue(new ApiCallback<Void>() {
                    @Override public void onSuccess(Void r) {

                        api.deleteShoppingListsByUser(auth, eq).enqueue(new ApiCallback<Void>() {
                            @Override public void onSuccess(Void r) {

                                api.deleteMealPlansByUser(auth, eq).enqueue(new ApiCallback<Void>() {
                                    @Override public void onSuccess(Void r) {

                                        api.deleteRecipesByUser(auth, eq).enqueue(new ApiCallback<Void>() {
                                            @Override public void onSuccess(Void r) {

                                                api.deleteIngredientsByUser(auth, eq).enqueue(new ApiCallback<Void>() {
                                                    @Override public void onSuccess(Void r) {

                                                        api.deleteProfile(auth, eq).enqueue(new ApiCallback<Void>() {
                                                            @Override public void onSuccess(Void r) {
                                                                logoutAndExit();
                                                            }

                                                            @Override public void onError(String e) {
                                                                logoutAndExit();
                                                            }
                                                        });

                                                    }
                                                    @Override public void onError(String e) {}
                                                });

                                            }
                                            @Override public void onError(String e) {}
                                        });

                                    }
                                    @Override public void onError(String e) {}
                                });

                            }
                            @Override public void onError(String e) {}
                        });

                    }
                    @Override public void onError(String e) {}
                });

            }

            @Override
            public void onError(String e) {
                Toast.makeText(ProfileActivity.this,
                        "Auth delete failed: " + e,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void logoutAndExit() {
        authManager.logout();

        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
