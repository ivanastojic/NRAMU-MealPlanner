package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealplanner.R;
import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.AuthResponse;
import com.example.mealplanner.models.LoginRequest;
import com.example.mealplanner.utils.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        if (authManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.openRegisterBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginBtn.setOnClickListener(v -> loginUser());

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput(email, password)) return;

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.getInstance()
                .getApi()
                .login(request)
                .enqueue(new ApiCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse response) {
                        String token = response.getAccessToken();

                        authManager.saveToken(token);
                        authManager.saveEmail(response.getUser().getEmail());
                        authManager.saveUserId(response.getUser().getId());

                        // 🔐 RLS CHECK – test da korisnik vidi samo svoje podatke
                        RetrofitClient.getInstance()
                                .getApi()
                                .getMyMealPlans("Bearer " + token)
                                .enqueue(new ApiCallback<Object>() {

                                    @Override
                                    public void onSuccess(Object data) {
                                        setLoading(false);
                                        goToNextScreen();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        setLoading(false);
                                        Toast.makeText(
                                                LoginActivity.this,
                                                "Prijava uspješna, ali provjera podataka nije uspjela.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);

                        if (errorMessage != null &&
                                (errorMessage.toLowerCase().contains("invalid")
                                        || errorMessage.toLowerCase().contains("credentials")
                                        || errorMessage.contains("401")
                                        || errorMessage.contains("400"))) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Netočna email adresa ili lozinka",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Greška pri prijavi. Pokušajte ponovno.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Unesite email");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Unesite lozinku");
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginBtn.setEnabled(!isLoading);
    }

    private void goToNextScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}