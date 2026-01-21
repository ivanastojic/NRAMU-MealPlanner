package com.example.mealplanner.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mealplanner.R;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.MealPlanRow;
import com.example.mealplanner.models.Profile;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.NotificationHelper;
import com.example.mealplanner.utils.ReminderScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private static final int REQ_POST_NOTIF = 5001;

    private SupabaseAPI api;
    private String auth;
    private String userId;

    private TextView tvTodayDate, tvTodayPlan;
    private TextView tvWelcome;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);

        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcome = findViewById(R.id.tvWelcome);

        showWelcomeFromPrefs();

        tvTodayDate = findViewById(R.id.tvTodayDate);
        tvTodayPlan = findViewById(R.id.tvTodayPlan);

        String todayPretty = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        if (tvTodayDate != null) tvTodayDate.setText("Danas, " + todayPretty);

        // Auth / API
        String token = authManager.getToken();
        userId = authManager.getUserId();

        if (token == null || token.trim().isEmpty()) {
            if (tvTodayPlan != null) tvTodayPlan.setText("Nema tokena - prijavi se ponovo.");
        } else {
            auth = "Bearer " + token;
            api = RetrofitClient.getInstance().getApi();

            fetchProfileAndCacheName(token);

            if (tvTodayPlan != null) tvTodayPlan.setText("Učitavam današnji plan...");
            loadTodayPlan();
        }

        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {

            bottomNav.setSelectedItemId(R.id.nav_home);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) return true;

                Intent intent = null;

                if (id == R.id.nav_recipes) {
                    intent = new Intent(MainActivity.this, RecipesListActivity.class);
                } else if (id == R.id.nav_planner) {
                    intent = new Intent(MainActivity.this, MyMealPlansActivity.class);
                } else if (id == R.id.nav_shopping) {
                    intent = new Intent(MainActivity.this, ShoppingListsActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            });
        }


        NotificationHelper.ensureChannel(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIF
                );
            }
        }


        ReminderScheduler.scheduleAllDailyReminders(this);
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        showWelcomeFromPrefs();

        String token = authManager.getToken();
        if (token != null && !token.trim().isEmpty()) {
            fetchProfileAndCacheName(token);
        }

        if (api == null && token != null && !token.trim().isEmpty()) {
            api = RetrofitClient.getInstance().getApi();
        }
        if (auth == null && token != null && !token.trim().isEmpty()) {
            auth = "Bearer " + token;
        }
        if (userId == null) {
            userId = authManager.getUserId();
        }

        if (api != null && auth != null) {
            if (tvTodayPlan != null) tvTodayPlan.setText("Učitavam današnji plan...");
            loadTodayPlan();
        }
    }

    private void showWelcomeFromPrefs() {
        if (tvWelcome == null) return;

        String fullName = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("full_name", "");

        if (fullName != null && !fullName.trim().isEmpty()) {
            tvWelcome.setText("Dobrodošli natrag, " + fullName + " 👋");
        } else {
            tvWelcome.setText("Dobrodošli natrag 👋");
        }
    }

    private void fetchProfileAndCacheName(String token) {
        RetrofitClient.getInstance()
                .getApi()
                .getMyProfileTyped("Bearer " + token)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            return;
                        }

                        Profile p = response.body().get(0);
                        String fullName = (p.getFullName() != null) ? p.getFullName().trim() : "";

                        if (!fullName.isEmpty()) {
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("full_name", fullName)
                                    .apply();

                            if (tvWelcome != null) {
                                tvWelcome.setText("Dobrodošli natrag, " + fullName + " 👋");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        // ništa
                    }
                });
    }

    private void loadTodayPlan() {
        String todayDb = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        api.getMyMealPlansAll(auth, "plan_date.asc").enqueue(new Callback<List<MealPlanRow>>() {
            @Override
            public void onResponse(Call<List<MealPlanRow>> call, Response<List<MealPlanRow>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (tvTodayPlan != null) tvTodayPlan.setText("Danas nema spremljenog plana.");
                    return;
                }

                List<MealPlanRow> todayPlans = new ArrayList<>();
                for (MealPlanRow p : response.body()) {
                    if (p == null || p.plan_date == null) continue;
                    if (p.plan_date.equals(todayDb)) todayPlans.add(p);
                }

                if (todayPlans.isEmpty()) {
                    if (tvTodayPlan != null) tvTodayPlan.setText("Danas nema spremljenog plana.");
                    return;
                }

                loadRecipeTitleMapAndBuild(todayPlans);
            }

            @Override
            public void onFailure(Call<List<MealPlanRow>> call, Throwable t) {
                if (tvTodayPlan != null) tvTodayPlan.setText("Greška pri učitavanju plana.");
            }
        });
    }

    private void loadRecipeTitleMapAndBuild(List<MealPlanRow> todayPlans) {
        if (userId == null || userId.trim().isEmpty()) {
            if (tvTodayPlan != null) tvTodayPlan.setText("Nema userId - prijavi se ponovo.");
            return;
        }

        api.getRecipesByUser(auth, "eq." + userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {

                Map<String, String> map = new HashMap<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Recipe r : response.body()) {
                        map.put(r.getId(), r.getTitle());
                    }
                }

                String text = buildStickyText(todayPlans, map);
                if (tvTodayPlan != null) tvTodayPlan.setText(text);
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                String text = buildStickyText(todayPlans, null);
                if (tvTodayPlan != null) tvTodayPlan.setText(text);
            }
        });
    }

    private String buildStickyText(List<MealPlanRow> todayPlans, Map<String, String> recipeTitleMap) {
        StringBuilder sb = new StringBuilder();

        for (MealPlanRow p : todayPlans) {
            String mealType = (p.meal_type == null) ? "" : p.meal_type.trim();
            String recipeId = (p.recipe_id == null) ? "" : p.recipe_id.trim();

            String title = recipeId;
            if (recipeTitleMap != null && recipeTitleMap.containsKey(recipeId)) {
                title = recipeTitleMap.get(recipeId);
            } else if (recipeId.isEmpty()) {
                title = "(bez recepta)";
            }

            sb.append(formatMealType(mealType)).append(title).append("\n");
        }

        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String formatMealType(String mealType) {
        String mt = mealType.toLowerCase(Locale.ROOT);

        if (mt.contains("breakfast") || mt.contains("doruc")) return " Doručak: ";
        if (mt.contains("lunch") || mt.contains("ruc")) return " Ručak: ";
        if (mt.contains("dinner") || mt.contains("vec")) return " Večera: ";
        if (mt.contains("snack") || mt.contains("uz")) return " Užina: ";

        return "🍽️ " + mealType + ": ";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
