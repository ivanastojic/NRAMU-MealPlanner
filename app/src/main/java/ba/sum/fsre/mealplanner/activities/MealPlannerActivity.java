package ba.sum.fsre.mealplanner.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.MealPlanAdapter;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.api.SupabaseAPI;
import ba.sum.fsre.mealplanner.api.ApiCallback;
import ba.sum.fsre.mealplanner.models.MealPlan;
import ba.sum.fsre.mealplanner.models.MealPlanRequest;
import ba.sum.fsre.mealplanner.models.Recipe;
import ba.sum.fsre.mealplanner.utils.AuthManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealPlannerActivity extends AppCompatActivity {

    private AuthManager authManager;
    private SupabaseAPI api;

    private TextView tvSelectedDate;
    private Button btnPickDate, btnSavePlan;
    private Spinner spMealType, spRecipe;
    private RecyclerView rvPlans;
    private TextView tvNoPlans;

    private String selectedDateIso = null;

    private List<Recipe> recipes = new ArrayList<>();
    private final HashMap<String, String> recipeIdToTitle = new HashMap<>();
    private MealPlanAdapter planAdapter;

    private String authToken;
    private String userId;

    private List<MealPlan> currentPlansForSelectedDate = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        authManager = new AuthManager(this);
        api = RetrofitClient.getInstance().getApi();

        authToken = "Bearer " + authManager.getToken();
        userId = authManager.getUserId();

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSavePlan = findViewById(R.id.btnSavePlan);
        spMealType = findViewById(R.id.spMealType);
        spRecipe = findViewById(R.id.spRecipe);
        rvPlans = findViewById(R.id.rvPlans);
        tvNoPlans = findViewById(R.id.tvNoPlans);

        setupMealTypeSpinner();
        setupRecycler();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSavePlan.setOnClickListener(v -> saveMealPlan());

        tvNoPlans.setVisibility(View.GONE);
        rvPlans.setVisibility(View.GONE);

        loadRecipes();
    }

    private void setupMealTypeSpinner() {
        String[] mealTypes = new String[]{"Breakfast", "Lunch", "Dinner"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_black,
                mealTypes
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
        spMealType.setAdapter(adapter);
    }

    private void setupRecycler() {
        planAdapter = new MealPlanAdapter(
                new ArrayList<>(),
                recipeIdToTitle,

                plan -> {
                    Intent i = new Intent(MealPlannerActivity.this, RecipeDetailsActivity.class);
                    i.putExtra("recipe_id", plan.recipe_id);
                    i.putExtra("recipe_title", recipeIdToTitle.get(plan.recipe_id));
                    i.putExtra("can_edit_ingredients", false);
                    startActivity(i);
                },

                new MealPlanAdapter.OnMealPlanMenuAction() {
                    @Override
                    public void onEdit(MealPlan plan) {
                        Toast.makeText(
                                MealPlannerActivity.this,
                                "Editing is available in My Plans.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onDelete(MealPlan plan) {
                        Toast.makeText(
                                MealPlannerActivity.this,
                                "Deleting is available in My Plans.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(planAdapter);
    }

    private void confirmDeletePlan(MealPlan plan) {
        new AlertDialog.Builder(this)
                .setTitle("Delete plan")
                .setMessage("Delete this meal plan?")
                .setPositiveButton("Delete", (d, w) -> deletePlan(plan))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlan(MealPlan plan) {
        api.deleteMealPlan(authToken, "eq." + plan.id)
                .enqueue(new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void response) {
                        Toast.makeText(
                                MealPlannerActivity.this,
                                "Plan deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadPlansForDate();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                MealPlannerActivity.this,
                                "Delete error: " + errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadRecipes() {
        Call<List<Recipe>> call = api.getRecipesByUser(authToken, "eq." + userId);
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipes = response.body();

                    recipeIdToTitle.clear();
                    List<String> titles = new ArrayList<>();
                    for (Recipe r : recipes) {
                        recipeIdToTitle.put(r.getId(), r.getTitle());
                        titles.add(r.getTitle());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            MealPlannerActivity.this,
                            R.layout.spinner_item_black,
                            titles
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
                    spRecipe.setAdapter(adapter);

                } else {
                    Toast.makeText(
                            MealPlannerActivity.this,
                            "Unable to load recipes",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(
                        MealPlannerActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void saveMealPlan() {
        if (selectedDateIso == null) {
            Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recipes == null || recipes.isEmpty()) {
            Toast.makeText(this, "No recipes available", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spRecipe.getSelectedItemPosition();
        if (pos < 0 || pos >= recipes.size()) {
            Toast.makeText(this, "Select a recipe", Toast.LENGTH_SHORT).show();
            return;
        }

        Recipe selectedRecipe = recipes.get(pos);
        String mealType = (String) spMealType.getSelectedItem();

        for (MealPlan p : currentPlansForSelectedDate) {
            if (p.meal_type != null && p.meal_type.equalsIgnoreCase(mealType)) {
                Toast.makeText(
                        this,
                        "A " + mealType + " already exists for this date. Edit it in My Plans.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        MealPlanRequest req = new MealPlanRequest(
                userId,
                selectedRecipe.getId(),
                selectedDateIso,
                mealType
        );

        Call<List<MealPlan>> call = api.insertMealPlan(authToken, req);
        call.enqueue(new Callback<List<MealPlan>>() {
            @Override
            public void onResponse(Call<List<MealPlan>> call, Response<List<MealPlan>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            MealPlannerActivity.this,
                            "Plan saved",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadPlansForDate();
                } else {
                    Toast.makeText(
                            MealPlannerActivity.this,
                            "Error while saving: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                Toast.makeText(
                        MealPlannerActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadPlansForDate() {
        if (selectedDateIso == null) return;

        Call<List<MealPlan>> call = api.getMealPlansByDate(
                authToken,
                "eq." + userId,
                "eq." + selectedDateIso,
                "created_at.desc"
        );

        call.enqueue(new Callback<List<MealPlan>>() {
            @Override
            public void onResponse(Call<List<MealPlan>> call, Response<List<MealPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<MealPlan> plans = response.body();
                    currentPlansForSelectedDate = plans;

                    planAdapter.setPlans(plans);

                    if (plans.isEmpty()) {
                        tvNoPlans.setVisibility(View.VISIBLE);
                        rvPlans.setVisibility(View.GONE);
                    } else {
                        tvNoPlans.setVisibility(View.GONE);
                        rvPlans.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(
                            MealPlannerActivity.this,
                            "Unable to load plans",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                Toast.makeText(
                        MealPlannerActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedDateIso = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            y,
                            (m + 1),
                            d
                    );
                    tvSelectedDate.setText("Date: " + selectedDateIso);
                    loadPlansForDate();
                },
                year, month, day
        );
        dialog.show();
    }
}

