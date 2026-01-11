package com.example.mealplanner.api;

import com.example.mealplanner.models.LoginRequest;
import com.example.mealplanner.models.AuthResponse;
import com.example.mealplanner.models.RegisterRequest;
import com.example.mealplanner.utils.Constants;
import com.example.mealplanner.models.Ingredient;
import com.example.mealplanner.models.Unit;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface SupabaseAPI {

    // LOGIN
    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    // REGISTER
    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // 🔐 RLS TEST – DOHVAT MEAL PLANOVA
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=*")
    Call<Object> getMyMealPlans(
            @Header("Authorization") String authToken
    );

    // 🔐 RLS TEST – DOHVAT PROFILA
    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/profiles?select=*")
    Call<Object> getMyProfile(
            @Header("Authorization") String authToken
    );

    // ===== INGREDIENTS =====

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json"
    })
    @POST("rest/v1/ingredients")
    Call<Void> insertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
    );

    // ===== UNITS =====

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/units?select=*")
    Call<List<Unit>> getUnits(
            @Header("Authorization") String authToken
    );


}
