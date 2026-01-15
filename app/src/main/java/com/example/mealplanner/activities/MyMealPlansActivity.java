package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.MyMealPlansGroupedAdapter;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.DayPlanGroup;
import com.example.mealplanner.models.MealPlanRow;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.utils.AuthManager;

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

    private MyMealPlansGroupedAdapter adapter;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String auth;
    private String userId;

    private List<MealPlanRow> rawPlans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_meal_plans);

        rvPlans = findViewById(R.id.rvPlans);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new MyMealPlansGroupedAdapter();
        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(adapter);

        authManager = new AuthManager(this);
        String token = authManager.getToken();
        userId = authManager.getUserId();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Nema tokena - prijavi se ponovo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();

        adapter.setOnDayClickListener(group -> openDayDetails(group.date));

        loadPlansAndTitles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlansAndTitles();
    }

    private void openDayDetails(String date) {
        Intent i = new Intent(this, DayPlansActivity.class);
        i.putExtra("plan_date", date);
        startActivity(i);
    }

    private void loadPlansAndTitles() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        api.getMyMealPlansAll(auth, "plan_date.asc").enqueue(new Callback<List<MealPlanRow>>() {
            @Override
            public void onResponse(Call<List<MealPlanRow>> call, Response<List<MealPlanRow>> response) {
                if (!response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(MyMealPlansActivity.this, "Greška (plans): " + response.code(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MyMealPlansActivity.this, "Network error (plans): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeTitleMap() {
        if (userId == null || userId.trim().isEmpty()) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, "Nema userId - prijavi se ponovo.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getRecipesByUser(auth, "eq." + userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                progress.setVisibility(View.GONE);

                Map<String, String> map = new HashMap<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Recipe r : response.body()) {
                        map.put(r.getId(), r.getTitle());
                    }
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
