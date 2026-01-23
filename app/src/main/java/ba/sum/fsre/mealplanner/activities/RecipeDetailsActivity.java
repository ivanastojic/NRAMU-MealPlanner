package ba.sum.fsre.mealplanner.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.IngredientDisplayAdapter;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.models.Ingredient;
import ba.sum.fsre.mealplanner.models.IngredientDisplay;
import ba.sum.fsre.mealplanner.models.RecipeIngredient;
import ba.sum.fsre.mealplanner.models.Unit;
import ba.sum.fsre.mealplanner.repositories.RecipeRepository;
import ba.sum.fsre.mealplanner.utils.AuthManager;
import ba.sum.fsre.mealplanner.utils.RecipeMapper;
import ba.sum.fsre.mealplanner.api.ApiCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsActivity extends AppCompatActivity {

    private IngredientDisplayAdapter adapter;
    private AuthManager auth;
    private RecipeRepository recipeRepository;

    private String recipeId;
    private boolean canEditIngredients = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details2);

        auth = new AuthManager(this);
        recipeRepository = new RecipeRepository();

        recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");

        canEditIngredients = getIntent().getBooleanExtra("can_edit_ingredients", false);

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        tvTitle.setText(title != null ? title : "Recipe details");

        RecyclerView rv = findViewById(R.id.rvIngredients);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new IngredientDisplayAdapter(new IngredientDisplayAdapter.OnIngredientActions() {
            @Override
            public void onEdit(IngredientDisplay ingredient) {
                if (!canEditIngredients) return;
                showEditIngredientDialog(ingredient);
            }

            @Override
            public void onDelete(IngredientDisplay ingredient) {
                if (!canEditIngredients) return;
                confirmDeleteIngredient(ingredient);
            }
        }, canEditIngredients);

        rv.setAdapter(adapter);

        loadAllForRecipe();
    }

    private void loadAllForRecipe() {
        if (recipeId == null) {
            toast("Missing recipe_id");
            return;
        }

        String token = auth.getToken();
        if (token == null) {
            toast("No token");
            return;
        }

        String authHeader = "Bearer " + token;

        RetrofitClient.getInstance().getApi().getIngredients(authHeader)
                .enqueue(new Callback<List<Ingredient>>() {
                    @Override
                    public void onResponse(Call<List<Ingredient>> call, Response<List<Ingredient>> ingRes) {
                        if (!ingRes.isSuccessful() || ingRes.body() == null) {
                            toast("Cannot fetch ingredients");
                            return;
                        }

                        RetrofitClient.getInstance().getApi().getUnits(authHeader)
                                .enqueue(new Callback<List<Unit>>() {
                                    @Override
                                    public void onResponse(Call<List<Unit>> call, Response<List<Unit>> unitRes) {
                                        if (!unitRes.isSuccessful() || unitRes.body() == null) {
                                            toast("Cannot fetch units");
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
                                                        toast("Error: " + t.getMessage());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(Call<List<Unit>> call, Throwable t) {
                                        toast("Error: " + t.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<List<Ingredient>> call, Throwable t) {
                        toast("Error: " + t.getMessage());
                    }
                });
    }

    private void showEditIngredientDialog(IngredientDisplay ingredient) {
        final EditText input = new EditText(this);
        input.setText(ingredient.line);

        new AlertDialog.Builder(this)
                .setTitle("Edit ingredient")
                .setMessage(ingredient.name)
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newLine = input.getText().toString().trim();
                    if (newLine.isEmpty()) {
                        toast("Cannot be empty");
                        return;
                    }

                    toast("Saving (TODO): " + newLine);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteIngredient(IngredientDisplay ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Delete \"" + ingredient.name + "\" from the recipe?")
                .setPositiveButton("Delete", (d, w) -> deleteIngredient(ingredient))
                .setNegativeButton("Cancel", null)
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
                        toast("Ingredient deleted");
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
