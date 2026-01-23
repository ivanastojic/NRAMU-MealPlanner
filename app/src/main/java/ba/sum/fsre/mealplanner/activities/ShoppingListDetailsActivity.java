package com.example.mealplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.ShoppingItemsAdapter;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.MealPlan;
import com.example.mealplanner.models.RecipeIngredient;
import com.example.mealplanner.models.ShoppingItem;
import com.example.mealplanner.models.Unit;
import com.example.mealplanner.utils.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListDetailsActivity extends AppCompatActivity {

    private TextView tvListTitle, tvListPeriod, tvEmptyItems;
    private Button btnRegenerate, btnDeleteList, btnMarkAllBought;
    private RecyclerView rvShoppingItems;
    private ProgressBar progressItems;

    private ShoppingItemsAdapter adapter;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String auth;
    private String userId;

    private String listId;
    private String dateFrom;
    private String dateTo;

    private final Map<String, String> ingredientNameById = new HashMap<>();
    private final Map<Integer, String> unitNameById = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_details);

        tvListTitle = findViewById(R.id.tvListTitle);
        tvListPeriod = findViewById(R.id.tvListPeriod);
        tvEmptyItems = findViewById(R.id.tvEmptyItems);

        btnRegenerate = findViewById(R.id.btnRegenerate);
        btnDeleteList = findViewById(R.id.btnDeleteList);
        btnMarkAllBought = findViewById(R.id.btnMarkAllBought);

        rvShoppingItems = findViewById(R.id.rvShoppingItems);
        progressItems = findViewById(R.id.progressItems);

        adapter = new ShoppingItemsAdapter();
        rvShoppingItems.setLayoutManager(new LinearLayoutManager(this));
        rvShoppingItems.setAdapter(adapter);

        authManager = new AuthManager(this);
        String token = authManager.getToken();
        userId = authManager.getUserId();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found – please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();
        listId = getIntent().getStringExtra("list_id");
        dateFrom = getIntent().getStringExtra("date_from");
        dateTo = getIntent().getStringExtra("date_to");

        if (listId == null || listId.trim().isEmpty()) {
            Toast.makeText(this, "LIST ID IS NULL", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvListTitle.setText("Shopping List");
        tvListPeriod.setText(dateFrom + " → " + dateTo);

        adapter.setListener((item, checked) -> toggleItem(item.id, checked));

        btnRegenerate.setOnClickListener(v -> regenerateFromMealPlans());
        btnDeleteList.setOnClickListener(v -> showDeleteConfirmDialog());
        btnMarkAllBought.setOnClickListener(v -> markAllBought());

        loadIngredientsAndUnits();
    }

    private void setLoading(boolean loading) {
        progressItems.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvShoppingItems.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean empty) {
        tvEmptyItems.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void loadIngredientsAndUnits() {
        setLoading(true);

        api.getIngredients(auth).enqueue(new Callback<List<Ingredient>>() {
            @Override
            public void onResponse(Call<List<Ingredient>> call, Response<List<Ingredient>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Ingredient i : response.body()) {
                        ingredientNameById.put(i.getId(), i.getName());
                    }
                }
                loadUnits();
            }

            @Override
            public void onFailure(Call<List<Ingredient>> call, Throwable t) {
                loadUnits();
            }
        });
    }

    private void loadUnits() {
        api.getUnits(auth).enqueue(new Callback<List<Unit>>() {
            @Override
            public void onResponse(Call<List<Unit>> call, Response<List<Unit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Unit u : response.body()) {
                        unitNameById.put(u.getId(), u.getName());
                    }
                }
                adapter.setMaps(ingredientNameById, unitNameById);
                loadItems();
            }

            @Override
            public void onFailure(Call<List<Unit>> call, Throwable t) {
                adapter.setMaps(ingredientNameById, unitNameById);
                loadItems();
            }
        });
    }

    private void loadItems() {
        setLoading(true);

        api.getShoppingItemsByList(auth, "eq." + listId, "created_at.asc")
                .enqueue(new Callback<List<ShoppingItem>>() {
                    @Override
                    public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            String err = "";
                            try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                            Toast.makeText(ShoppingListDetailsActivity.this,
                                    "Unable to load items: " + response.code() + "\n" + err,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<ShoppingItem> items = response.body();
                        adapter.setItems(items);

                        boolean empty = (items == null || items.isEmpty());
                        showEmpty(empty);

                        if (empty) {
                            regenerateFromMealPlans();
                            return;
                        }

                        boolean allChecked = true;
                        for (ShoppingItem it : items) {
                            if (!it.is_checked) { allChecked = false; break; }
                        }

                        setListCompleted(allChecked);
                        btnMarkAllBought.setVisibility(allChecked ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(ShoppingListDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void regenerateFromMealPlans() {
        setLoading(true);

        api.deleteShoppingItemsByList(auth, "eq." + listId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    setLoading(false);
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(ShoppingListDetailsActivity.this,
                            "Unable to delete old items: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                generateItemsFromMealPlans();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ShoppingListDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateItemsFromMealPlans() {
        List<String> planDateFilters = new ArrayList<>();
        planDateFilters.add("gte." + dateFrom);
        planDateFilters.add("lte." + dateTo);

        api.getMealPlansByRange(auth, "eq." + userId, planDateFilters, "plan_date.asc")
                .enqueue(new Callback<List<MealPlan>>() {
                    @Override
                    public void onResponse(Call<List<MealPlan>> call, Response<List<MealPlan>> response) {
                        if (!response.isSuccessful()) {
                            setLoading(false);
                            String err = "";
                            try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                            Toast.makeText(ShoppingListDetailsActivity.this,
                                    "Meal plans error: " + response.code() + "\n" + err,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<MealPlan> plans = response.body();
                        if (plans == null || plans.isEmpty()) {
                            setLoading(false);
                            adapter.setItems(new ArrayList<>());
                            showEmpty(true);
                            setListCompleted(false);
                            btnMarkAllBought.setVisibility(View.GONE);
                            Toast.makeText(ShoppingListDetailsActivity.this,
                                    "No meal plans for this week.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Integer> recipeCount = new HashMap<>();
                        for (MealPlan p : plans) {
                            if (p.recipe_id == null) continue;
                            recipeCount.put(p.recipe_id, recipeCount.getOrDefault(p.recipe_id, 0) + 1);
                        }

                        if (recipeCount.isEmpty()) {
                            setLoading(false);
                            adapter.setItems(new ArrayList<>());
                            showEmpty(true);
                            setListCompleted(false);
                            btnMarkAllBought.setVisibility(View.GONE);
                            Toast.makeText(ShoppingListDetailsActivity.this,
                                    "No recipes in the plan.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String in = "in.(" + String.join(",", recipeCount.keySet()) + ")";
                        loadRecipeIngredientsAndInsert(in, recipeCount);
                    }

                    @Override
                    public void onFailure(Call<List<MealPlan>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(ShoppingListDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRecipeIngredientsAndInsert(String recipeIdInFilter, Map<String, Integer> recipeCount) {
        api.getRecipeIngredientsByRecipeIds(auth, recipeIdInFilter)
                .enqueue(new Callback<List<RecipeIngredient>>() {
                    @Override
                    public void onResponse(Call<List<RecipeIngredient>> call, Response<List<RecipeIngredient>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            setLoading(false);
                            String err = "";
                            try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                            Toast.makeText(ShoppingListDetailsActivity.this,
                                    "Ingredients error: " + response.code() + "\n" + err,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Double> sumMap = new HashMap<>();
                        Map<String, String> ingIdMap = new HashMap<>();
                        Map<String, Integer> unitIdMap = new HashMap<>();

                        for (RecipeIngredient ri : response.body()) {
                            String recipeId = ri.getRecipeId();
                            String ingId = ri.getIngredientId();
                            int unitId = ri.getUnitId();
                            double q = ri.getQuantity();

                            int times = recipeCount.getOrDefault(recipeId, 1);
                            double add = q * times;

                            String key = ingId + "|" + unitId;
                            sumMap.put(key, sumMap.getOrDefault(key, 0.0) + add);
                            ingIdMap.put(key, ingId);
                            unitIdMap.put(key, unitId);
                        }

                        List<ShoppingItem> items = new ArrayList<>();
                        for (String key : sumMap.keySet()) {
                            ShoppingItem it = new ShoppingItem();
                            it.shopping_list_id = listId;
                            it.user_id = userId;
                            it.ingredient_id = ingIdMap.get(key);
                            it.unit_id = unitIdMap.get(key);
                            it.quantity = round2(sumMap.get(key));
                            it.is_checked = false;
                            it.source_date_from = dateFrom;
                            it.source_date_to = dateTo;
                            items.add(it);
                        }

                        api.insertShoppingItems(auth, items).enqueue(new Callback<List<ShoppingItem>>() {
                            @Override
                            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> resp) {
                                if (!resp.isSuccessful()) {
                                    setLoading(false);
                                    String err = "";
                                    try { err = resp.errorBody() != null ? resp.errorBody().string() : ""; } catch (Exception ignored) {}
                                    Toast.makeText(ShoppingListDetailsActivity.this,
                                            "Unable to save items: " + resp.code() + "\n" + err,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Toast.makeText(ShoppingListDetailsActivity.this,
                                        "Shopping list regenerated",
                                        Toast.LENGTH_SHORT).show();

                                setListCompleted(false);
                                loadItems();
                            }

                            @Override
                            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                                setLoading(false);
                                Toast.makeText(ShoppingListDetailsActivity.this,
                                        "Network error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<RecipeIngredient>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(ShoppingListDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private void toggleItem(String itemId, boolean checked) {
        Map<String, Object> body = new HashMap<>();
        body.put("is_checked", checked);

        api.updateShoppingItem(auth, "eq." + itemId, body).enqueue(new Callback<List<ShoppingItem>>() {
            @Override
            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                if (!response.isSuccessful()) {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(ShoppingListDetailsActivity.this,
                            "Update error: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                loadItems();
            }

            @Override
            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                Toast.makeText(ShoppingListDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllBought() {
        setLoading(true);

        Map<String, Object> body = new HashMap<>();
        body.put("is_checked", true);

        api.updateShoppingItemsByList(auth, "eq." + listId, body).enqueue(new Callback<List<ShoppingItem>>() {
            @Override
            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(ShoppingListDetailsActivity.this,
                            "Unable to mark all items: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                setListCompleted(true);
                Toast.makeText(ShoppingListDetailsActivity.this,
                        "All items marked as bought",
                        Toast.LENGTH_SHORT).show();
                loadItems();
            }

            @Override
            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ShoppingListDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setListCompleted(boolean completed) {
        Map<String, Object> body = new HashMap<>();
        body.put("is_completed", completed);

        api.updateShoppingList(auth, "eq." + listId, body).enqueue(new Callback<List<com.example.mealplanner.models.ShoppingList>>() {
            @Override
            public void onResponse(Call<List<com.example.mealplanner.models.ShoppingList>> call,
                                   Response<List<com.example.mealplanner.models.ShoppingList>> response) {
            }

            @Override
            public void onFailure(Call<List<com.example.mealplanner.models.ShoppingList>> call, Throwable t) {
            }
        });
    }

    private void deleteList() {
        api.deleteShoppingList(auth, "eq." + listId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Toast.makeText(ShoppingListDetailsActivity.this,
                            "Delete error: " + response.code() + "\n" + err,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ShoppingListDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete list?")
                .setMessage("Are you sure you want to delete this shopping list? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> deleteList())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
