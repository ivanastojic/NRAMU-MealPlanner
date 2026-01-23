package com.example.mealplanner.repositories;

import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;

import java.util.List;

import retrofit2.Callback;



public class RecipeRepository {

    public void updateRecipeTitle(
            String token,
            String recipeId,
            String newTitle,
            Callback<List<Recipe>> callback
    ) {
        Recipe body = new Recipe();
        body.setTitle(newTitle);

        RetrofitClient.getInstance()
                .getApi()
                .updateRecipe("Bearer " + token, "eq." + recipeId, body)
                .enqueue(callback);
    }

    public void deleteRecipe(
            String token,
            String recipeId,
            Callback<Void> callback
    ) {
        RetrofitClient.getInstance()
                .getApi()
                .deleteRecipe("Bearer " + token, "eq." + recipeId)
                .enqueue(callback);
    }

    public void createRecipe(
            String token,
            Recipe recipe,
            Callback<List<Recipe>> callback
    ) {
        RetrofitClient.getInstance()
                .getApi()
                .createRecipe("Bearer " + token, recipe)
                .enqueue(callback);
    }

    public void addIngredientToRecipe(
            String token,
            RecipeIngredient recipeIngredient,
            Callback<List<RecipeIngredient>> callback
    ) {
        RetrofitClient.getInstance()
                .getApi()
                .addIngredientToRecipe("Bearer " + token, recipeIngredient)
                .enqueue(callback);
    }

    public void deleteRecipeIngredientById(
            String token,
            String recipeIngredientId,
            Callback<Void> callback
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
