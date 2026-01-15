package com.example.mealplanner.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealplanner.R;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.MealPlan;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.utils.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditMealPlanActivity extends AppCompatActivity {

    private TextView tvDate;
    private TextView tvMealTypeLabel;
    private TextView tvMealType;
    private Spinner spRecipe;
    private Button btnSaveChanges;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String authToken;

    private String planId;
    private String planDate;
    private String mealType;
    private String oldRecipeId;

    private List<Recipe> recipes = new ArrayList<>();
    private final Map<String, String> recipeIdToTitle = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meal_plan);

        tvDate = findViewById(R.id.tvDate);
        tvMealTypeLabel = findViewById(R.id.tvMealTypeLabel);
        tvMealType = findViewById(R.id.tvMealType);
        spRecipe = findViewById(R.id.spRecipe);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        authManager = new AuthManager(this);
        api = RetrofitClient.getInstance().getApi();

        String token = authManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Nema tokena - prijavi se ponovo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        authToken = "Bearer " + token;

        // Intent extras
        planId = getIntent().getStringExtra("plan_id");
        planDate = getIntent().getStringExtra("plan_date");
        mealType = getIntent().getStringExtra("meal_type");
        oldRecipeId = getIntent().getStringExtra("recipe_id");

        if (planId == null || planId.trim().isEmpty()) {
            Toast.makeText(this, "Nedostaje plan_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // read-only prikaz
        tvDate.setText("Datum: " + (planDate != null ? planDate : "-"));
        tvMealTypeLabel.setText("Obrok:");
        tvMealType.setText(mealType != null ? mealType : "-");

        loadRecipes();

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadRecipes() {
        String userId = authManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Nema userId - prijavi se ponovo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api.getRecipesByUser(authToken, "eq." + userId).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EditMealPlanActivity.this, "Ne mogu učitati recepte", Toast.LENGTH_SHORT).show();
                    return;
                }

                recipes = response.body();
                List<String> titles = new ArrayList<>();
                recipeIdToTitle.clear();

                int preselectIndex = 0;

                for (int i = 0; i < recipes.size(); i++) {
                    Recipe r = recipes.get(i);
                    recipeIdToTitle.put(r.getId(), r.getTitle());
                    titles.add(r.getTitle());

                    if (oldRecipeId != null && oldRecipeId.equals(r.getId())) {
                        preselectIndex = i;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        EditMealPlanActivity.this,
                        R.layout.spinner_item_black,
                        titles
                );
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
                spRecipe.setAdapter(adapter);
                spRecipe.setSelection(preselectIndex);
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(EditMealPlanActivity.this, "Greška mreže: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        int pos = spRecipe.getSelectedItemPosition();
        if (pos < 0 || pos >= recipes.size()) {
            Toast.makeText(this, "Odaberi recept", Toast.LENGTH_SHORT).show();
            return;
        }

        String newRecipeId = recipes.get(pos).getId();

        if (oldRecipeId != null && oldRecipeId.equals(newRecipeId)) {
            Toast.makeText(this, "Nema promjena.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSaveChanges.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("recipe_id", newRecipeId);

        api.updateMealPlan(authToken, "eq." + planId, body).enqueue(new Callback<List<MealPlan>>() {
            @Override
            public void onResponse(Call<List<MealPlan>> call, Response<List<MealPlan>> response) {
                btnSaveChanges.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(EditMealPlanActivity.this, "Plan ažuriran ✅", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditMealPlanActivity.this, "Greška: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                btnSaveChanges.setEnabled(true);
                Toast.makeText(EditMealPlanActivity.this, "Mrežna greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
