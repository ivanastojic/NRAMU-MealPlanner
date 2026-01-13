package com.example.mealplanner.repositories;

import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.Unit;

import java.util.List;

public class IngredientRepository {

    // ✅ POST ingredient -> vraća representation (List<Ingredient>)
    public void addIngredient(String token, Ingredient ingredient, ApiCallback<List<Ingredient>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .insertIngredient("Bearer " + token, ingredient)
                .enqueue(callback);
    }

    // ✅ GET units
    public void getUnits(String token, ApiCallback<List<Unit>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .getUnits("Bearer " + token)
                .enqueue(callback);
    }

    // ✅ GET ingredients
    public void getIngredients(String token, ApiCallback<List<Ingredient>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .getIngredients("Bearer " + token)
                .enqueue(callback);
    }
}
