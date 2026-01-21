package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.example.mealplanner.models.RegisterRequest;
import com.example.mealplanner.utils.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerBtn, backToLoginBtn;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new AuthManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerBtn = findViewById(R.id.registerBtn);
        backToLoginBtn = findViewById(R.id.backToLoginBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        registerBtn.setOnClickListener(v -> registerUser());

        backToLoginBtn.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!validateInput(fullName, email, password, confirmPassword)) return;

        setLoading(true);

        RegisterRequest request = new RegisterRequest(email, password, fullName);

        RetrofitClient.getInstance()
                .getApi()
                .register(request)
                .enqueue(new ApiCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse response) {
                        setLoading(false);

                        if (response == null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registracija nije uspjela",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (response.getAccessToken() == null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registracija uspješna, provjerite email da aktivirate račun",
                                    Toast.LENGTH_LONG
                            ).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }

                        authManager.saveToken(response.getAccessToken());
                        authManager.saveEmail(response.getUser().getEmail());
                        authManager.saveUserId(response.getUser().getId());


                        Toast.makeText(
                                RegisterActivity.this,
                                "Registracija uspješna",
                                Toast.LENGTH_SHORT
                        ).show();

                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }


                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                RegisterActivity.this,
                                errorMessage != null ? errorMessage : "Greška pri registraciji",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {

        if (fullName.isEmpty()) {
            fullNameInput.setError("Unesite ime i prezime");
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Unesite email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Neispravan email");
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Unesite lozinku");
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Lozinka mora imati najmanje 6 znakova");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Lozinke se ne podudaraju");
            return false;
        }

        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerBtn.setEnabled(!isLoading);
    }
}
