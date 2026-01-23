package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.RecipeAdapter;
import ba.sum.fsre.mealplanner.api.ApiCallback;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.models.Recipe;
import ba.sum.fsre.mealplanner.repositories.RecipeRepository;
import ba.sum.fsre.mealplanner.utils.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class RecipesListActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private AuthManager auth;
    private RecipeRepository recipeRepo;

    private BottomNavigationView bottomNav;

    private static final int REQ_EDIT_RECIPE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_list);

        auth = new AuthManager(this);
        recipeRepo = new RecipeRepository();

        findViewById(R.id.fabAddRecipe).setOnClickListener(v -> {
            startActivity(new Intent(RecipesListActivity.this, AddRecipeActivity.class));
            overridePendingTransition(0, 0);
        });

        setupBottomNav();

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(
                recipe -> {
                    Intent i = new Intent(this, RecipeDetailsActivity.class);
                    i.putExtra("recipe_id", recipe.getId());
                    i.putExtra("recipe_title", recipe.getTitle());
                    startActivity(i);
                    overridePendingTransition(0, 0);
                },
                new RecipeAdapter.OnRecipeMenuAction() {
                    @Override
                    public void onEdit(Recipe recipe) {
                        Intent i = new Intent(RecipesListActivity.this, EditRecipeActivity.class);
                        i.putExtra("recipe_id", recipe.getId());
                        i.putExtra("recipe_title", recipe.getTitle());
                        startActivityForResult(i, REQ_EDIT_RECIPE);
                        overridePendingTransition(0, 0);
                    }

                    @Override
                    public void onDelete(Recipe recipe) {
                        showDeleteConfirmDialog(recipe);
                    }
                }
        );

        rv.setAdapter(adapter);

        loadRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_recipes);

        loadRecipes();
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_recipes);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recipes) return true;

            Intent i = null;

            if (id == R.id.nav_home) i = new Intent(this, MainActivity.class);
            else if (id == R.id.nav_planner) i = new Intent(this, MyMealPlansActivity.class);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // edit already refreshes here, but onResume refreshes anyway
        if (requestCode == REQ_EDIT_RECIPE && resultCode == RESULT_OK) {
            loadRecipes();
        }
    }

    private void loadRecipes() {
        String userId = auth.getUserId();
        String token = auth.getToken();

        if (userId == null || token == null) {
            Toast.makeText(this, "Missing user/token (login?)", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApi()
                .getRecipesByUser("Bearer " + token, "eq." + userId)
                .enqueue(new ApiCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> response) {
                        adapter.setItems(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(RecipesListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmDialog(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Delete recipe?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteRecipe(recipe.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecipe(String recipeId) {
        String token = auth.getToken();
        if (token == null) return;

        recipeRepo.deleteRecipe(token, recipeId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                Toast.makeText(RecipesListActivity.this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                loadRecipes();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RecipesListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
