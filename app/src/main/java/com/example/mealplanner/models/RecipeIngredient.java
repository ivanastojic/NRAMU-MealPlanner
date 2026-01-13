package com.example.mealplanner.models;

import com.google.gson.annotations.SerializedName;

public class RecipeIngredient {

    @SerializedName("recipe_id")
    private String recipeId;

    @SerializedName("ingredient_id")
    private String ingredientId;

    private double quantity;

    @SerializedName("unit_id")
    private int unitId;

    private String note;

    public RecipeIngredient() {
        // potreban za Gson
    }

    public RecipeIngredient(String recipeId, String ingredientId, double quantity, int unitId, String note) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.note = note;
    }

    public String getRecipeId() { return recipeId; }
    public String getIngredientId() { return ingredientId; }
    public double getQuantity() { return quantity; }
    public int getUnitId() { return unitId; }
    public String getNote() { return note; }
}
