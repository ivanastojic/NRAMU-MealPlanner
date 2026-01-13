package com.example.mealplanner.api;

import com.example.mealplanner.models.AuthResponse;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.LoginRequest;
import com.example.mealplanner.models.Recipe;
import com.example.mealplanner.models.RecipeIngredient;
import com.example.mealplanner.models.RegisterRequest;
import com.example.mealplanner.models.Unit;
import com.example.mealplanner.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;

public interface SupabaseAPI {

    // ================= AUTH =================
// UPDATE recipe title (PATCH) - filter po id
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/recipes")
    Call<List<Recipe>> updateRecipe(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Recipe recipe
    );

    // DELETE recipe - filter po id
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/recipes")
    Call<Void> deleteRecipe(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // ================= RLS TEST =================

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=*")
    Call<Object> getMyMealPlans(
            @Header("Authorization") String authToken
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/profiles?select=*")
    Call<Object> getMyProfile(
            @Header("Authorization") String authToken
    );

    // ================= INGREDIENTS =================

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/ingredients")
    Call<List<Ingredient>> insertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
    );
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation,resolution=merge-duplicates"
    })
    @POST("rest/v1/ingredients?on_conflict=user_id,name")
    Call<List<Ingredient>> upsertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
    );

    // GET all ingredients (za mapiranje id -> name)
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/ingredients?select=*")
    Call<List<Ingredient>> getIngredients(
            @Header("Authorization") String authToken
    );

    // ================= UNITS =================

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/units?select=*")
    Call<List<Unit>> getUnits(
            @Header("Authorization") String authToken
    );

    // ================= RECIPES =================

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Prefer: return=representation"
    })
    @POST("rest/v1/recipes")
    Call<List<Recipe>> createRecipe(
            @Header("Authorization") String authToken,
            @Body Recipe recipe
    );

    // GET recipes by user_id (filter ide kao eq.<id>)
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/recipes?select=*")
    Call<List<Recipe>> getRecipesByUser(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter
    );

    // ================= RECIPE INGREDIENTS =================

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("rest/v1/recipe_ingredients")
    Call<Void> addIngredientToRecipe(
            @Header("Authorization") String authToken,
            @Body RecipeIngredient recipeIngredient
    );

    // GET recipe_ingredients by recipe_id (filter eq.<id>)
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/recipe_ingredients?select=*")
    Call<List<RecipeIngredient>> getRecipeIngredientsByRecipeId(
            @Header("Authorization") String authToken,
            @Query("recipe_id") String recipeIdFilter
    );

    // ================= MEAL PLANS =================

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Prefer: return=representation"
    })
    @POST("rest/v1/meal_plans")
    Call<List<com.example.mealplanner.models.MealPlan>> insertMealPlan(
            @Header("Authorization") String authToken,
            @Body com.example.mealplanner.models.MealPlanRequest body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=*")
    Call<List<com.example.mealplanner.models.MealPlan>> getMealPlansByDate(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter,   // "eq.<userId>"
            @Query("plan_date") String dateFilter,   // "eq.<YYYY-MM-DD>"
            @Query("order") String order
    );

}
