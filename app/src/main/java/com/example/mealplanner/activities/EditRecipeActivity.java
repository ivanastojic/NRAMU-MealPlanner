package com.example.mealplanner.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.EditableIngredientAdapter;
import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.EditableRecipeIngredient;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;
import com.example.mealplanner.models.Unit;
import com.example.mealplanner.repositories.RecipeRepository;
import com.example.mealplanner.utils.AuthManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {

    private AuthManager auth;
    private RecipeRepository recipeRepo;

    private String recipeId;

    private EditText etTitle;
    private RecyclerView rv;
    private MaterialButton btnSave;

    private EditableIngredientAdapter adapter;
    private EditText etIngName, etQty, etNote;
    private Spinner spUnit;
    private MaterialButton btnAddIngredient;

    private ArrayAdapter<String> unitAdapter;


    private final List<EditableRecipeIngredient> items = new ArrayList<>();
    private final List<Unit> units = new ArrayList<>();
    private final Map<String, String> ingredientNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        auth = new AuthManager(this);
        recipeRepo = new RecipeRepository();

        recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");

        if (recipeId == null) {
            toast("Nedostaje recipe_id");
            finish();
            return;
        }

        etTitle = findViewById(R.id.etEditRecipeTitle);
        rv = findViewById(R.id.rvIngredientsEdit);
        btnSave = findViewById(R.id.btnSaveRecipeEdit);

        etIngName = findViewById(R.id.etIngredientName);
        etQty = findViewById(R.id.etQuantity);
        etNote = findViewById(R.id.etNote);
        spUnit = findViewById(R.id.spUnit);
        btnAddIngredient = findViewById(R.id.btnAddIngredientEdit);

        if (title != null) {
            etTitle.setText(title);
        }

        unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        adapter = new EditableIngredientAdapter((item, position) -> {

            String token = auth.getToken();
            if (token == null) {
                toast("Nema tokena");
                return;
            }

            if (item.recipeIngredientId.startsWith("NEW_")) {
                adapter.removeAt(position);
                return;
            }

            RetrofitClient.getInstance().getApi()
                    .deleteRecipeIngredientById(
                            "Bearer " + token,
                            "eq." + item.recipeIngredientId
                    )
                    .enqueue(new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void r) {
                            adapter.removeAt(position);
                            toast("Sastojak obrisan");
                        }

                        @Override
                        public void onError(String e) {
                            toast("Greška pri brisanju: " + e);
                        }
                    });
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnAddIngredient.setOnClickListener(v -> addIngredientToRecipe());
        btnSave.setOnClickListener(v -> saveAll());

        loadIngredientNames();
        loadUnits();
    }


    private void loadUnits() {
        String token = auth.getToken();
        if (token == null) return;

        RetrofitClient.getInstance().getApi()
                .getUnits("Bearer " + token)
                .enqueue(new ApiCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> response) {

                        units.clear();
                        unitAdapter.clear();

                        if (response != null) {
                            units.addAll(response);
                            for (Unit u : response) {
                                unitAdapter.add(u.getName());
                            }
                        }

                        unitAdapter.notifyDataSetChanged();
                        adapter.setUnits(units);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast(errorMessage);
                    }
                });
    }

    private void loadIngredientNames() {
        String token = auth.getToken();
        if (token == null) return;

        RetrofitClient.getInstance().getApi()
                .getIngredients("Bearer " + token)
                .enqueue(new ApiCallback<List<Ingredient>>() {
                    @Override
                    public void onSuccess(List<Ingredient> response) {
                        ingredientNameMap.clear();
                        if (response != null) {
                            for (Ingredient i : response) {
                                ingredientNameMap.put(i.getId(), i.getName());
                            }
                        }
                        loadRecipeIngredients();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast(errorMessage);
                    }
                });
    }

    private void loadRecipeIngredients() {
        String token = auth.getToken();
        if (token == null) return;

        RetrofitClient.getInstance().getApi()
                .getRecipeIngredientsByRecipeId(
                        "Bearer " + token,
                        "eq." + recipeId
                )
                .enqueue(new ApiCallback<List<RecipeIngredient>>() {
                    @Override
                    public void onSuccess(List<RecipeIngredient> response) {
                        items.clear();

                        if (response != null) {
                            for (RecipeIngredient ri : response) {
                                String name = ingredientNameMap.get(ri.getIngredientId());
                                if (name == null) name = "Nepoznato";

                                items.add(new EditableRecipeIngredient(
                                        ri.getId(),
                                        ri.getIngredientId(),
                                        name,
                                        ri.getQuantity(),
                                        ri.getUnitId(),
                                        ri.getNote()
                                ));
                            }
                        }

                        adapter.setItems(items);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast(errorMessage);
                    }
                });
    }


    private void saveAll() {
        String token = auth.getToken();
        if (token == null) return;

        String newTitle = etTitle.getText().toString().trim();
        if (newTitle.isEmpty()) {
            toast("Naziv ne smije biti prazan");
            return;
        }

        recipeRepo.updateRecipeTitle(
                token,
                recipeId,
                newTitle,
                new ApiCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> response) {
                        saveIngredients(token);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        toast(errorMessage);
                    }
                }
        );
    }

    private void addIngredientToRecipe() {

        String token = auth.getToken();
        String userId = auth.getUserId();

        if (token == null || userId == null) {
            toast("Nema session-a");
            return;
        }

        String name = etIngName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty()) {
            toast("Upiši sastojak");
            return;
        }

        if (qtyStr.isEmpty()) {
            toast("Upiši količinu");
            return;
        }

        if (units.isEmpty()) {
            toast("Jedinice nisu učitane");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (Exception e) {
            toast("Količina nije broj");
            return;
        }

        int pos = spUnit.getSelectedItemPosition();
        if (pos < 0 || pos >= units.size()) {
            toast("Odaberi jedinicu");
            return;
        }

        Unit unit = units.get(pos);

        Ingredient ingredient = new Ingredient(userId, name, null);

        RetrofitClient.getInstance().getApi()
                .upsertIngredient("Bearer " + token, ingredient)
                .enqueue(new ApiCallback<List<Ingredient>>() {
                    @Override
                    public void onSuccess(List<Ingredient> res) {

                        if (res == null || res.isEmpty()) {
                            toast("Greška kod sastojka");
                            return;
                        }

                        String ingredientId = res.get(0).getId();

                        ingredientNameMap.put(ingredientId, name);

                        RecipeIngredient ri = new RecipeIngredient(
                                recipeId,
                                ingredientId,
                                qty,
                                unit.getId(),
                                note
                        );

                        RetrofitClient.getInstance().getApi()
                                .addIngredientToRecipe("Bearer " + token, ri)
                                .enqueue(new ApiCallback<List<RecipeIngredient>>() {
                                    @Override
                                    public void onSuccess(List<RecipeIngredient> ignored) {

                                        toast("Dodano: " + name);

                                        loadRecipeIngredients();

                                        etIngName.setText("");
                                        etQty.setText("");
                                        etNote.setText("");
                                        spUnit.setSelection(0);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        toast(error);
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        toast(error);
                    }
                });
    }


    private void saveIngredients(String token) {

        for (EditableRecipeIngredient i : adapter.getItems()) {

            if (i.recipeIngredientId.startsWith("NEW_")) continue;

            RecipeIngredient ri = new RecipeIngredient();
            ri.setQuantity(i.quantity);
            ri.setUnitId(i.unitId);
            ri.setNote(i.note);

            RetrofitClient.getInstance().getApi()
                    .updateRecipeIngredient(
                            "Bearer " + token,
                            "eq." + i.recipeIngredientId,
                            ri
                    )
                    .enqueue(new ApiCallback<List<RecipeIngredient>>() {
                        @Override public void onSuccess(List<RecipeIngredient> r) {}
                        @Override public void onError(String e) {
                            toast(e);
                        }
                    });
        }

        toast("Promjene spremljene");
        setResult(RESULT_OK);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
