package com.example.mealplanner.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.MealPlanAdapter;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.MealPlan;
import com.example.mealplanner.models.MealPlanRequest;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.utils.AuthManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        authManager = new AuthManager(this);
        api = RetrofitClient.getInstance().getApi();

        authToken = "Bearer " + authManager.getToken();
        userId = authManager.getUserId();

        System.out.println("AUTH TOKEN = " + authToken);

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

        // U početku: sakrij poruku, sakrij listu dok nema datuma
        tvNoPlans.setVisibility(View.GONE);
        rvPlans.setVisibility(View.GONE);

        // učitaj recepte odmah
        loadRecipes();
    }

    private void setupMealTypeSpinner() {
        String[] mealTypes = new String[]{"Breakfast", "Lunch", "Dinner"};

        // ✅ Koristi tvoje layout-e da tekst ne bude bijel
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_black,  // <-- tvoj item layout
                mealTypes
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black); // <-- dropdown layout
        spMealType.setAdapter(adapter);
    }

    private void setupRecycler() {
        planAdapter = new MealPlanAdapter(new ArrayList<>(), recipeIdToTitle);
        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(planAdapter);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", y, (m + 1), d);
                    tvSelectedDate.setText("Datum: " + selectedDateIso);

                    // čim odabere datum, učitaj planove
                    loadPlansForDate();
                },
                year, month, day
        );
        dialog.show();
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

                    // ✅ Koristi tvoje layout-e da tekst u spinneru ne bude bijel
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            MealPlannerActivity.this,
                            R.layout.spinner_item_black, // <-- tvoj item layout
                            titles
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black); // <-- dropdown layout
                    spRecipe.setAdapter(adapter);

                } else {
                    Toast.makeText(MealPlannerActivity.this, "Ne mogu učitati recepte", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(MealPlannerActivity.this, "Greška mreže: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMealPlan() {
        if (selectedDateIso == null) {
            Toast.makeText(this, "Odaberi datum", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recipes == null || recipes.isEmpty()) {
            Toast.makeText(this, "Nema recepata za odabir", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spRecipe.getSelectedItemPosition();
        if (pos < 0 || pos >= recipes.size()) {
            Toast.makeText(this, "Odaberi recept", Toast.LENGTH_SHORT).show();
            return;
        }

        Recipe selectedRecipe = recipes.get(pos);
        String mealType = (String) spMealType.getSelectedItem();

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
                    Toast.makeText(MealPlannerActivity.this, "Spremljeno!", Toast.LENGTH_SHORT).show();
                    loadPlansForDate();
                } else {
                    Toast.makeText(MealPlannerActivity.this, "Greška pri spremanju: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                Toast.makeText(MealPlannerActivity.this, "Greška mreže: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    planAdapter.setPlans(plans);

                    // prikaz poruke / liste
                    if (plans.isEmpty()) {
                        tvNoPlans.setVisibility(View.VISIBLE);
                        rvPlans.setVisibility(View.GONE);
                    } else {
                        tvNoPlans.setVisibility(View.GONE);
                        rvPlans.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(MealPlannerActivity.this, "Ne mogu učitati planove", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                Toast.makeText(MealPlannerActivity.this, "Greška mreže: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
