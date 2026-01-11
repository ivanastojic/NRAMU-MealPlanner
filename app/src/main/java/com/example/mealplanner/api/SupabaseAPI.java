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

public interface SupabaseAPI {

    // ================= AUTH =================

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
            "Content-Type: application/json"
    })
    @POST("rest/v1/ingredients")
    Call<Void> insertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
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

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("rest/v1/recipe_ingredients")
    Call<Void> addIngredientToRecipe(
            @Header("Authorization") String authToken,
            @Body RecipeIngredient recipeIngredient
    );
}
