package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.MealPlanAdapter;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.api.SupabaseAPI;
import ba.sum.fsre.mealplanner.models.MealPlan;
import ba.sum.fsre.mealplanner.models.Recipe;
import ba.sum.fsre.mealplanner.utils.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayPlansActivity extends AppCompatActivity {

    private TextView tvDate;
    private RecyclerView rvPlans;
    private ProgressBar progress;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String authToken;
    private String userId;
    private String date;

    private final HashMap<String, String> recipeIdToTitle = new HashMap<>();
    private MealPlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_plans);

        tvDate = findViewById(R.id.tvDate);
        rvPlans = findViewById(R.id.rvPlans);
        progress = findViewById(R.id.progress);

        date = getIntent().getStringExtra("plan_date");
        tvDate.setText("Date: " + date);

        authManager = new AuthManager(this);
        api = RetrofitClient.getInstance().getApi();
        authToken = "Bearer " + authManager.getToken();
        userId = authManager.getUserId();

        adapter = new MealPlanAdapter(
                new ArrayList<>(),
                recipeIdToTitle,
                plan -> openRecipeDetails(plan.recipe_id),
                new MealPlanAdapter.OnMealPlanMenuAction() {
                    @Override
                    public void onEdit(MealPlan plan) {
                        openEditPlan(plan);
                    }

                    @Override
                    public void onDelete(MealPlan plan) {
                        confirmDelete(plan);
                    }
                }
        );

        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(adapter);

        loadRecipeTitlesThenPlans();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipeTitlesThenPlans();
    }

    private void loadRecipeTitlesThenPlans() {
        progress.setVisibility(View.VISIBLE);

        api.getRecipesByUser(authToken, "eq." + userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                recipeIdToTitle.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Recipe r : response.body()) {
                        recipeIdToTitle.put(r.getId(), r.getTitle());
                    }
                }
                loadPlans();
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                loadPlans();
            }
        });
    }

    private void loadPlans() {
        api.getMealPlansByDate(authToken, "eq." + userId, "eq." + date, "created_at.asc")
                .enqueue(new Callback<List<MealPlan>>() {
                    @Override
                    public void onResponse(Call<List<MealPlan>> call, Response<List<MealPlan>> response) {
                        progress.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(DayPlansActivity.this,
                                    "Error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        adapter.setPlans(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(DayPlansActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openRecipeDetails(String recipeId) {
        Intent i = new Intent(this, RecipeDetailsActivity.class);
        i.putExtra("recipe_id", recipeId);
        i.putExtra("recipe_title", recipeIdToTitle.get(recipeId));
        i.putExtra("can_edit_ingredients", false);

        startActivity(i);
    }

    private void openEditPlan(MealPlan plan) {
        Intent i = new Intent(this, EditMealPlanActivity.class);
        i.putExtra("plan_id", plan.id);
        i.putExtra("plan_date", plan.plan_date);
        i.putExtra("meal_type", plan.meal_type);
        i.putExtra("recipe_id", plan.recipe_id);
        startActivity(i);
    }

    private void confirmDelete(MealPlan plan) {
        new AlertDialog.Builder(this)
                .setTitle("Delete meal?")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Delete", (d, w) -> delete(plan))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void delete(MealPlan plan) {
        progress.setVisibility(View.VISIBLE);
        api.deleteMealPlan(authToken, "eq." + plan.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DayPlansActivity.this,
                            "Meal deleted",
                            Toast.LENGTH_SHORT).show();
                    loadRecipeTitlesThenPlans();
                } else {
                    Toast.makeText(DayPlansActivity.this,
                            "Delete error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(DayPlansActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
