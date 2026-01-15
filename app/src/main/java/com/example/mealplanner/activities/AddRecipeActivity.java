package com.example.mealplanner.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealplanner.R;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;
import com.example.mealplanner.models.Unit;
import com.example.mealplanner.utils.AuthManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private AuthManager auth;

    private EditText etTitle, etIngName, etQty, etNote;
    private Spinner spUnit;
    private MaterialButton btnAddIng, btnSave;
    private TextView tvAddedCount;

    private final List<Unit> units = new ArrayList<>();
    private ArrayAdapter<String> unitNamesAdapter;

    private final List<AddedIngredient> added = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        auth = new AuthManager(this);

        etTitle = findViewById(R.id.etRecipeTitle);
        etIngName = findViewById(R.id.etIngredientName);
        etQty = findViewById(R.id.etQuantity);
        etNote = findViewById(R.id.etNote);
        spUnit = findViewById(R.id.spUnit);
        btnAddIng = findViewById(R.id.btnAddIngredient);
        btnSave = findViewById(R.id.btnSaveRecipe);
        tvAddedCount = findViewById(R.id.tvAddedCount);

        unitNamesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        unitNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitNamesAdapter);

        loadUnits();

        btnAddIng.setOnClickListener(v -> addLocalIngredient());
        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void loadUnits() {
        String token = auth.getToken();
        if (token == null) { toast("Nema tokena"); return; }

        RetrofitClient.getInstance().getApi()
                .getUnits("Bearer " + token)
                .enqueue(new ApiCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> response) {
                        units.clear();
                        unitNamesAdapter.clear();

                        if (response != null) {
                            units.addAll(response);
                            for (Unit u : response) {
                                unitNamesAdapter.add(u.getName());
                            }
                        }

                        unitNamesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast("Units error: " + errorMessage);
                    }
                });
    }

    private void addLocalIngredient() {
        String name = etIngName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty()) { toast("Upiši sastojak"); return; }
        if (qtyStr.isEmpty()) { toast("Upiši količinu"); return; }
        if (units.isEmpty()) { toast("Jedinice nisu učitane"); return; }

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (Exception e) {
            toast("Količina nije broj");
            return;
        }

        int pos = spUnit.getSelectedItemPosition();
        if (pos < 0 || pos >= units.size()) { toast("Odaberi jedinicu"); return; }

        Unit unit = units.get(pos);

        added.add(new AddedIngredient(name, qty, unit.getId(), note));
        tvAddedCount.setText("Dodano: " + added.size() + " sastojaka");

        toast("Dodano: " + name + " (" + qty + " " + unit.getName() + ")");

        etIngName.setText("");
        etQty.setText("");
        etNote.setText("");
    }

    private void saveRecipe() {
        String token = auth.getToken();
        String userId = auth.getUserId();

        if (token == null || userId == null) {
            toast("Nema session-a");
            return;
        }

        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            toast("Upiši naziv recepta");
            return;
        }
        if (added.isEmpty()) {
            toast("Dodaj barem 1 sastojak");
            return;
        }

        Recipe recipe = new Recipe(title, userId);

        RetrofitClient.getInstance().getApi()
                .createRecipe("Bearer " + token, recipe)
                .enqueue(new ApiCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> response) {
                        String recipeId = response.get(0).getId();
                        insertNextIngredient(token, userId, recipeId, 0);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast("Create recipe error: " + errorMessage);
                    }
                });
    }

    private void insertNextIngredient(String token, String userId, String recipeId, int i) {
        if (i >= added.size()) {
            toast("Recept spremljen!");
            finish();
            return;
        }

        AddedIngredient ai = added.get(i);

        Ingredient ingredient = new Ingredient(userId, ai.name, null);

        RetrofitClient.getInstance().getApi()
                .upsertIngredient("Bearer " + token, ingredient)
                .enqueue(new ApiCallback<List<Ingredient>>() {
                    @Override
                    public void onSuccess(List<Ingredient> response) {
                        String ingredientId = response.get(0).getId();

                        RecipeIngredient ri = new RecipeIngredient(
                                recipeId,
                                ingredientId,
                                ai.quantity,
                                ai.unitId,
                                ai.note
                        );

                        RetrofitClient.getInstance().getApi()
                                .addIngredientToRecipe("Bearer " + token, ri)
                                .enqueue(new ApiCallback<List<RecipeIngredient>>() {
                                    @Override
                                    public void onSuccess(List<RecipeIngredient> ignored) {
                                        insertNextIngredient(token, userId, recipeId, i + 1);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        toast("Link error: " + errorMessage);
                                    }
                                });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast("Insert ingredient error: " + errorMessage);
                    }
                });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static class AddedIngredient {
        final String name;
        final double quantity;
        final int unitId;
        final String note;

        AddedIngredient(String name, double quantity, int unitId, String note) {
            this.name = name;
            this.quantity = quantity;
            this.unitId = unitId;
            this.note = note;
        }
    }
}
