package com.example.mealplanner.models;

import com.google.gson.annotations.SerializedName;

public class ShoppingItem {

    @SerializedName("id")
    public String id;

    @SerializedName("shopping_list_id")
    public String shopping_list_id;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("ingredient_id")
    public String ingredient_id;

    @SerializedName("unit_id")
    public int unit_id;

    @SerializedName("quantity")
    public double quantity;

    @SerializedName("is_checked")
    public boolean is_checked;

    @SerializedName("source_date_from")
    public String source_date_from;

    @SerializedName("source_date_to")
    public String source_date_to;

    @SerializedName("created_at")
    public String created_at;
}