package com.example.mealplanner.api;

import com.example.mealplanner.models.LoginRequest;
import com.example.mealplanner.models.AuthResponse;
import com.example.mealplanner.utils.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SupabaseAPI {

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Authorization: Bearer " + Constants.ANON_KEY
    })
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);
}
