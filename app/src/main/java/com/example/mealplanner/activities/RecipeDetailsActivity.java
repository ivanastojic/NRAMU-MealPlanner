package com.example.mealplanner.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.example.mealplanner.repositories.RecipeRepository;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.RecipeMapper;
import com.example.mealplanner.api.ApiCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsActivity extends AppCompatActivity {

    private IngredientDisplayAdapter adapter;
    private AuthManager auth;
    private RecipeRepository recipeRepository;

    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details2);

        auth = new AuthManager(this);
        recipeRepository = new RecipeRepository();

        recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        tvTitle.setText(title != null ? title : "Detalji recepta");

        RecyclerView rv = findViewById(R.id.rvIngredients);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new IngredientDisplayAdapter(new IngredientDisplayAdapter.OnIngredientLongClick() {
            @Override
            public void onEdit(IngredientDisplay ingredient) {
            }

            @Override
            public void onDelete(IngredientDisplay ingredient) {
                confirmDeleteIngredient(ingredient);
            }
        });

        rv.setAdapter(adapter);

        loadAllForRecipe();
    }

    // ================= LOAD DATA =================

    private void loadAllForRecipe() {
        if (recipeId == null) {
            toast("Nedostaje recipe_id");
            return;
        }

        String token = auth.getToken();
        if (token == null) {
            toast("Nema tokena");
            return;
        }

        String authHeader = "Bearer " + token;

        RetrofitClient.getInstance().getApi().getIngredients(authHeader)
                .enqueue(new Callback<List<Ingredient>>() {
                    @Override
                    public void onResponse(Call<List<Ingredient>> call, Response<List<Ingredient>> ingRes) {
                        if (!ingRes.isSuccessful() || ingRes.body() == null) {
                            toast("Ne mogu dohvatiti ingredients");
                            return;
                        }

                        RetrofitClient.getInstance().getApi().getUnits(authHeader)
                                .enqueue(new Callback<List<Unit>>() {
                                    @Override
                                    public void onResponse(Call<List<Unit>> call, Response<List<Unit>> unitRes) {
                                        if (!unitRes.isSuccessful() || unitRes.body() == null) {
                                            toast("Ne mogu dohvatiti units");
                                            return;
                                        }

                                        RetrofitClient.getInstance().getApi()
                                                .getRecipeIngredientsByRecipeId(authHeader, "eq." + recipeId)
                                                .enqueue(new Callback<List<RecipeIngredient>>() {
                                                    @Override
                                                    public void onResponse(Call<List<RecipeIngredient>> call,
                                                                           Response<List<RecipeIngredient>> riRes) {
                                                        if (!riRes.isSuccessful() || riRes.body() == null) {
                                                            adapter.setItems(null);
                                                            return;
                                                        }

                                                        List<IngredientDisplay> display =
                                                                RecipeMapper.mapToDisplay(
                                                                        riRes.body(),
                                                                        ingRes.body(),
                                                                        unitRes.body()
                                                                );

                                                        adapter.setItems(display);
                                                    }

                                                    @Override
                                                    public void onFailure(Call<List<RecipeIngredient>> call, Throwable t) {
                                                        toast("Greška: " + t.getMessage());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(Call<List<Unit>> call, Throwable t) {
                                        toast("Greška: " + t.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<List<Ingredient>> call, Throwable t) {
                        toast("Greška: " + t.getMessage());
                    }
                });
    }

    // ================= DELETE =================

    private void confirmDeleteIngredient(IngredientDisplay ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Obriši sastojak")
                .setMessage("Obrisati \"" + ingredient.name + "\" iz recepta?")
                .setPositiveButton("Obriši", (d, w) -> deleteIngredient(ingredient))
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void deleteIngredient(IngredientDisplay ingredient) {
        String token = auth.getToken();
        if (token == null) return;

        recipeRepository.deleteRecipeIngredientById(
                token,
                ingredient.recipeIngredientId,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void response) {
                        toast("Sastojak obrisan");
                        adapter.setItems(null);
                        loadAllForRecipe();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast("Delete error: " + errorMessage);
                    }
                }
        );
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
