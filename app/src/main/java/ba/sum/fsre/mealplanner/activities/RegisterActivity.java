package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.api.ApiCallback;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.models.AuthResponse;
import ba.sum.fsre.mealplanner.models.RegisterRequest;
import ba.sum.fsre.mealplanner.utils.AuthManager;

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
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (response.getAccessToken() == null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration successful. Check your email to activate your account.",
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
                                "Registration successful",
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
                                errorMessage != null ? errorMessage : "Registration error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {

        if (fullName.isEmpty()) {
            fullNameInput.setError("Enter your full name");
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Enter your email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email");
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Enter your password");
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerBtn.setEnabled(!isLoading);
    }
}
