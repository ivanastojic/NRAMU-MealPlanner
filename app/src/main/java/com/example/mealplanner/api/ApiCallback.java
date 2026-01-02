package com.example.mealplanner.api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            String error = "HTTP " + response.code();
            try {
                if (response.errorBody() != null) {
                    error += " | " + response.errorBody().string();
                }
            } catch (Exception ignored) {}
            onError(error);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onError("Network error: " + t.getMessage());
    }

    public abstract void onSuccess(T response);
    public abstract void onError(String errorMessage);
}
