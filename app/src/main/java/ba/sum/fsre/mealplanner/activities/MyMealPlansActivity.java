package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.MyMealPlansGroupedAdapter;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.api.SupabaseAPI;
import ba.sum.fsre.mealplanner.models.DayPlanGroup;
import ba.sum.fsre.mealplanner.models.MealPlanRow;
import ba.sum.fsre.mealplanner.models.Recipe;
import ba.sum.fsre.mealplanner.utils.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyMealPlansActivity extends AppCompatActivity {

    private RecyclerView rvPlans;
    private ProgressBar progress;
    private TextView tvEmpty;

    private FloatingActionButton fabAddPlan;

    private MyMealPlansGroupedAdapter adapter;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String auth;
    private String userId;

    private List<MealPlanRow> rawPlans = new ArrayList<>();

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_meal_plans);

        rvPlans = findViewById(R.id.rvPlans);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddPlan = findViewById(R.id.fabAddPlan);

        adapter = new MyMealPlansGroupedAdapter();
        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(adapter);

        authManager = new AuthManager(this);
        String token = authManager.getToken();
        userId = authManager.getUserId();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found – please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();

        adapter.setOnDayClickListener(group -> openDayDetails(group.date));

        if (fabAddPlan != null) {
            fabAddPlan.setOnClickListener(v -> {
                startActivity(new Intent(MyMealPlansActivity.this, MealPlannerActivity.class));
                overridePendingTransition(0, 0);
            });
        }

        setupBottomNav();
        loadPlansAndTitles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_planner);
        loadPlansAndTitles();
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_planner);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_planner) return true;

            Intent i = null;

            if (id == R.id.nav_home) i = new Intent(this, MainActivity.class);
            else if (id == R.id.nav_recipes) i = new Intent(this, RecipesListActivity.class);
            else if (id == R.id.nav_shopping) i = new Intent(this, ShoppingListsActivity.class);
            else if (id == R.id.nav_profile) i = new Intent(this, ProfileActivity.class);

            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void openDayDetails(String date) {
        Intent i = new Intent(this, DayPlansActivity.class);
        i.putExtra("plan_date", date);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    private void loadPlansAndTitles() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        api.getMyMealPlansAll(auth, "plan_date.asc").enqueue(new Callback<List<MealPlanRow>>() {
            @Override
            public void onResponse(Call<List<MealPlanRow>> call, Response<List<MealPlanRow>> response) {
                if (!response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(MyMealPlansActivity.this,
                            "Error (plans): " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                rawPlans = response.body();
                if (rawPlans == null || rawPlans.isEmpty()) {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    adapter.setItems(new ArrayList<>());
                    return;
                }

                loadRecipeTitleMap();
            }

            @Override
            public void onFailure(Call<List<MealPlanRow>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(MyMealPlansActivity.this,
                        "Network error (plans): " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeTitleMap() {
        if (userId == null || userId.trim().isEmpty()) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, "No userId – please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getRecipesByUser(auth, "eq." + userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                progress.setVisibility(View.GONE);

                Map<String, String> map = new HashMap<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Recipe r : response.body()) map.put(r.getId(), r.getTitle());
                }

                adapter.setRecipeTitleMap(map);
                adapter.setItems(groupByDate(rawPlans));
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                adapter.setItems(groupByDate(rawPlans));
            }
        });
    }

    private List<DayPlanGroup> groupByDate(List<MealPlanRow> plans) {
        Map<String, DayPlanGroup> map = new LinkedHashMap<>();
        for (MealPlanRow p : plans) {
            if (p.plan_date == null) continue;
            DayPlanGroup g = map.get(p.plan_date);
            if (g == null) {
                g = new DayPlanGroup(p.plan_date);
                map.put(p.plan_date, g);
            }
            g.plans.add(p);
        }
        return new ArrayList<>(map.values());
    }
}
