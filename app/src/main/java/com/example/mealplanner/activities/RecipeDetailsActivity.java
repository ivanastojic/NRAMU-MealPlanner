package com.example.mealplanner.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.IngredientDisplayAdapter;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.IngredientDisplay;
import com.example.mealplanner.models.RecipeIngredient;
import com.example.mealplanner.models.Unit;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.RecipeMapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsActivity extends AppCompatActivity {

    private IngredientDisplayAdapter adapter;
    private AuthManager auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details2);

        auth = new AuthManager(this);

        String recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        tvTitle.setText(title != null ? title : "Detalji recepta");

        RecyclerView rv = findViewById(R.id.rvIngredients);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new IngredientDisplayAdapter();
        rv.setAdapter(adapter);

        loadAllForRecipe(recipeId);
    }

    private void loadAllForRecipe(String recipeId) {
        if (recipeId == null) {
            Toast.makeText(this, "Nedostaje recipe_id", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = auth.getToken();
        if (token == null) {
            Toast.makeText(this, "Nema token (login?)", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;

        // 1) ingredients
        RetrofitClient.getInstance().getApi().getIngredients(authHeader)
                .enqueue(new Callback<List<Ingredient>>() {
                    @Override
                    public void onResponse(Call<List<Ingredient>> call, Response<List<Ingredient>> ingRes) {
                        if (!ingRes.isSuccessful() || ingRes.body() == null) {
                            Toast.makeText(RecipeDetailsActivity.this, "Ne mogu dohvatiti ingredients", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 2) units
                        RetrofitClient.getInstance().getApi().getUnits(authHeader)
                                .enqueue(new Callback<List<Unit>>() {
                                    @Override
                                    public void onResponse(Call<List<Unit>> call, Response<List<Unit>> unitRes) {
                                        if (!unitRes.isSuccessful() || unitRes.body() == null) {
                                            Toast.makeText(RecipeDetailsActivity.this, "Ne mogu dohvatiti units", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // 3) recipe_ingredients for recipe
                                        RetrofitClient.getInstance().getApi()
                                                .getRecipeIngredientsByRecipeId(authHeader, "eq." + recipeId)
                                                .enqueue(new Callback<List<RecipeIngredient>>() {
                                                    @Override
                                                    public void onResponse(Call<List<RecipeIngredient>> call, Response<List<RecipeIngredient>> riRes) {
                                                        if (!riRes.isSuccessful() || riRes.body() == null) {
                                                            Toast.makeText(RecipeDetailsActivity.this, "Nema sastojaka", Toast.LENGTH_SHORT).show();
                                                            adapter.setItems(null);
                                                            return;
                                                        }

                                                        List<IngredientDisplay> display = RecipeMapper.mapToDisplay(
                                                                riRes.body(),
                                                                ingRes.body(),
                                                                unitRes.body()
                                                        );

                                                        adapter.setItems(display);
                                                    }

                                                    @Override
                                                    public void onFailure(Call<List<RecipeIngredient>> call, Throwable t) {
                                                        Toast.makeText(RecipeDetailsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(Call<List<Unit>> call, Throwable t) {
                                        Toast.makeText(RecipeDetailsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<List<Ingredient>> call, Throwable t) {
                        Toast.makeText(RecipeDetailsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
