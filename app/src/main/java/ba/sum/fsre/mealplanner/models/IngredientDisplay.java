package com.example.mealplanner.models;

public class IngredientDisplay {

    public String recipeIngredientId;
    public String ingredientId;
    public String name;
    public String line;

    public IngredientDisplay(
            String recipeIngredientId,
            String ingredientId,
            String name,
            String line
    ) {
        this.recipeIngredientId = recipeIngredientId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.line = line;
    }
}
