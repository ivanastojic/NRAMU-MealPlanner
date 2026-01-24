package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.api.ApiCallback;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.models.AuthResponse;
import ba.sum.fsre.mealplanner.models.LoginRequest;
import ba.sum.fsre.mealplanner.utils.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private ProgressBar progressBar;
    private AuthManager authManager;
    private TextView tvPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);
        if (!hasUserConsent()) {
            showConsentDialog();
        }

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
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);
    }

    private void setupListeners() {
        loginBtn.setOnClickListener(v -> loginUser());

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        if (tvPrivacyPolicy != null) {
            tvPrivacyPolicy.setOnClickListener(v -> {
                Intent i = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://docs.google.com/document/d/1OxhV_sCTChVrxdL9BwiMvwhMZSQ5RyCTOVScHWnWZWI/edit")
                );
                startActivity(i);
            });
        }
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
                                                "Login successful, but data verification failed.",
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
                                    "Incorrect email address or password",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login error. Please try again.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Enter email");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Enter password");
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

    private boolean hasUserConsent() {
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("user_consent", false);
    }

    private void saveUserConsent(boolean accepted) {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("user_consent", accepted)
                .apply();
    }

    private void showConsentDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("User Consent")
                .setMessage(
                        "By using this application, you agree that:\n\n" +
                                "• Your basic profile data (name, email, avatar) is stored\n" +
                                "• Recipes, meal plans and shopping lists are stored\n" +
                                "• Data is used for app functionality and anonymously for project research purposes\n\n" +
                                "Do you accept these terms?"
                )
                .setCancelable(false)
                .setPositiveButton("I Accept", (d, w) -> {
                    saveUserConsent(true);
                })
                .setNegativeButton("I Do Not Accept", (d, w) -> {
                    finish();
                })
                .show();
    }

}
