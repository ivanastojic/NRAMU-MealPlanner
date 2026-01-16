package com.example.mealplanner.models;

import com.google.gson.annotations.SerializedName;
public class ShoppingList {

    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("date_from")
    public String date_from;

    @SerializedName("date_to")
    public String date_to;

    @SerializedName("is_completed")
    public boolean is_completed;

    public String created_at;

}