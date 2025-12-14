package com.example.mealplanner.api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            onSuccess(response.body());
        } else {
            onError("Greška: " + response.code());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onError("Greška mreže: " + t.getMessage());
    }

    public abstract void onSuccess(T response);
    public abstract void onError(String errorMessage);
}
