package ba.sum.fsre.mealplanner.repositories;

import ba.sum.fsre.mealplanner.api.ApiCallback;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.models.Ingredient;
import ba.sum.fsre.mealplanner.models.Unit;

import java.util.List;

public class IngredientRepository {

    public void addIngredient(String token, Ingredient ingredient, ApiCallback<List<Ingredient>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .insertIngredient("Bearer " + token, ingredient)
                .enqueue(callback);
    }

    public void getUnits(String token, ApiCallback<List<Unit>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .getUnits("Bearer " + token)
                .enqueue(callback);
    }

    public void getIngredients(String token, ApiCallback<List<Ingredient>> callback) {
        RetrofitClient.getInstance()
                .getApi()
                .getIngredients("Bearer " + token)
                .enqueue(callback);
    }
}
