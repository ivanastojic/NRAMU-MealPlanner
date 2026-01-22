package com.example.mealplanner.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealplanner.R;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.ShoppingList;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateShoppingListActivity extends AppCompatActivity {

    private TextInputEditText etAnyDate;
    private TextView tvWeekRange;
    private MaterialButton btnGenerateWeekly;
    private ProgressBar progressGenerate;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String auth;
    private String userId;

    private String selectedMonday;
    private String selectedSunday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_shopping_list);

        etAnyDate = findViewById(R.id.etAnyDate);
        tvWeekRange = findViewById(R.id.tvWeekRange);
        btnGenerateWeekly = findViewById(R.id.btnGenerateWeekly);
        progressGenerate = findViewById(R.id.progressGenerate);

        authManager = new AuthManager(this);
        String token = authManager.getToken();
        userId = authManager.getUserId();

        if (token == null || token.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();

        Calendar today = Calendar.getInstance();
        setWeekFromCalendar(today);
        etAnyDate.setText(formatDate(today));

        etAnyDate.setOnClickListener(v -> pickDate());
        btnGenerateWeekly.setOnClickListener(v -> generateForSelectedWeek());
    }

    private void setLoading(boolean loading) {
        progressGenerate.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerateWeekly.setEnabled(!loading);
        etAnyDate.setEnabled(!loading);
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth);
            etAnyDate.setText(formatDate(picked));
            setWeekFromCalendar(picked);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatDate(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
    }

    private void setWeekFromCalendar(Calendar c) {
        int day = c.get(Calendar.DAY_OF_WEEK);
        int diffToMonday = (day == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - day);
        Calendar monday = (Calendar) c.clone();
        monday.add(Calendar.DAY_OF_MONTH, diffToMonday);

        Calendar sunday = (Calendar) monday.clone();
        sunday.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedMonday = sdf.format(monday.getTime());
        selectedSunday = sdf.format(sunday.getTime());

        tvWeekRange.setText("Week: " + selectedMonday + " → " + selectedSunday);
    }

    private void generateForSelectedWeek() {
        if (selectedMonday == null || selectedSunday == null) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        api.getShoppingListByWeek(
                auth,
                "eq." + userId,
                "eq." + selectedMonday,
                "eq." + selectedSunday
        ).enqueue(new Callback<List<ShoppingList>>() {
            @Override
            public void onResponse(Call<List<ShoppingList>> call, Response<List<ShoppingList>> response) {
                if (!response.isSuccessful()) {
                    setLoading(false);
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(GenerateShoppingListActivity.this,
                            "Error checking list: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                List<ShoppingList> found = response.body();
                if (found != null && !found.isEmpty()) {
                    ShoppingList existing = found.get(0);
                    Toast.makeText(GenerateShoppingListActivity.this,
                            "Shopping list for this week already exists.",
                            Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    openDetails(existing.id, existing.date_from, existing.date_to);
                    finish();
                    return;
                }

                createWeeklyList();
            }

            @Override
            public void onFailure(Call<List<ShoppingList>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(GenerateShoppingListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createWeeklyList() {
        ShoppingList body = new ShoppingList();
        body.user_id = userId;
        body.date_from = selectedMonday;
        body.date_to = selectedSunday;
        body.is_completed = false;

        api.createShoppingList(auth, body).enqueue(new Callback<List<ShoppingList>>() {
            @Override
            public void onResponse(Call<List<ShoppingList>> call, Response<List<ShoppingList>> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(GenerateShoppingListActivity.this,
                            "Unable to create shopping list: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                ShoppingList created = response.body().get(0);
                Toast.makeText(GenerateShoppingListActivity.this, "Shopping list created.", Toast.LENGTH_SHORT).show();
                NotificationHelper.showShoppingReady(GenerateShoppingListActivity.this);
                openDetails(created.id, created.date_from, created.date_to);
                finish();
            }

            @Override
            public void onFailure(Call<List<ShoppingList>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(GenerateShoppingListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDetails(String listId, String from, String to) {
        Intent i = new Intent(this, ShoppingListDetailsActivity.class);
        i.putExtra("list_id", listId);
        i.putExtra("date_from", from);
        i.putExtra("date_to", to);
        startActivity(i);
    }
}