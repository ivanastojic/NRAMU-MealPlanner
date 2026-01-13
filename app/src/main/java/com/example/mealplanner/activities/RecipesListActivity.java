package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.RecipeAdapter;
import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.repositories.RecipeRepository;
import com.example.mealplanner.utils.AuthManager;

import java.util.List;

public class RecipesListActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private AuthManager auth;
    private RecipeRepository recipeRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_list);

        auth = new AuthManager(this);
        recipeRepo = new RecipeRepository();

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(
                recipe -> {
                    // klik → detalji
                    Intent i = new Intent(this, RecipeDetailsActivity.class);
                    i.putExtra("recipe_id", recipe.getId());
                    i.putExtra("recipe_title", recipe.getTitle());
                    startActivity(i);
                },
                recipe -> {
                    // long klik → edit / delete
                    showRecipeOptionsDialog(recipe);
                }
        );

        rv.setAdapter(adapter);
        loadRecipes();
    }

    private void loadRecipes() {
        String userId = auth.getUserId();
        String token = auth.getToken();

        if (userId == null || token == null) {
            Toast.makeText(this, "Nema user/token (login?)", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApi()
                .getRecipesByUser("Bearer " + token, "eq." + userId)
                .enqueue(new ApiCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> response) {
                        if (response == null || response.isEmpty()) {
                            Toast.makeText(RecipesListActivity.this, "Nema recepata", Toast.LENGTH_SHORT).show();
                        }
                        adapter.setItems(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(RecipesListActivity.this, "Greška: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ===== LONG CLICK MENU =====

    private void showRecipeOptionsDialog(Recipe recipe) {
        String[] options = {"Uredi naziv", "Obriši"};

        new AlertDialog.Builder(this)
                .setTitle(recipe.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showEditTitleDialog(recipe);
                    else showDeleteConfirmDialog(recipe);
                })
                .show();
    }

    private void showEditTitleDialog(Recipe recipe) {
        final EditText input = new EditText(this);
        input.setText(recipe.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("Uredi naziv recepta")
                .setView(input)
                .setPositiveButton("Spremi", (d, w) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        updateRecipeTitle(recipe.getId(), newTitle);
                    }
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void showDeleteConfirmDialog(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Obrisati recept?")
                .setMessage("Ova radnja se ne može vratiti.")
                .setPositiveButton("Obriši", (d, w) -> deleteRecipe(recipe.getId()))
                .setNegativeButton("Odustani", null)
                .show();
    }

    // ===== BACKEND =====

    private void updateRecipeTitle(String recipeId, String newTitle) {
        String token = auth.getToken();
        if (token == null) return;

        recipeRepo.updateRecipeTitle(token, recipeId, newTitle, new ApiCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> response) {
                Toast.makeText(RecipesListActivity.this, "Recept ažuriran", Toast.LENGTH_SHORT).show();
                loadRecipes();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RecipesListActivity.this, "Greška: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRecipe(String recipeId) {
        String token = auth.getToken();
        if (token == null) return;

        recipeRepo.deleteRecipe(token, recipeId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                Toast.makeText(RecipesListActivity.this, "Recept obrisan", Toast.LENGTH_SHORT).show();
                loadRecipes();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RecipesListActivity.this, "Greška: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
