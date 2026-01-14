package com.example.mealplanner.repositories;

import com.example.mealplanner.api.ApiCallback;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;

import java.util.List;

public class RecipeRepository {
    public void updateRecipeTitle(String token, String recipeId, String newTitle, ApiCallback<List<Recipe>> callback) {
        Recipe body = new Recipe();
        body.setTitle(newTitle);

        RetrofitClient.getInstance()
                .getApi()
                .updateRecipe("Bearer " + token, "eq." + recipeId, body)
                .enqueue(callback);
    }

    public void deleteRecipe(String token, String recipeId, ApiCallback<Void> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .deleteRecipe("Bearer " + token, "eq." + recipeId)
                .enqueue(callback);
    }


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

    public void deleteRecipeIngredientById(
            String token,
            String recipeIngredientId,
            ApiCallback<Void> callback
    ) {
        RetrofitClient.getInstance()
                .getApi()
                .deleteRecipeIngredientById(
                        "Bearer " + token,
                        "eq." + recipeIngredientId
                )
                .enqueue(callback);
    }

}
