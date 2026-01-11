package com.example.mealplanner.repositories;

import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;

import java.util.List;

public class RecipeRepository {

    public void createRecipe(String token, Recipe recipe, ApiCallback<List<Recipe>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .createRecipe("Bearer " + token, recipe)
                .enqueue(callback);
    }

    public void addIngredientToRecipe(String token,
                                      RecipeIngredient recipeIngredient,
                                      ApiCallback<Void> callback) {

        RetrofitClient.getInstance()
                .getApi()
                .addIngredientToRecipe("Bearer " + token, recipeIngredient)
                .enqueue(callback);
    }
}
