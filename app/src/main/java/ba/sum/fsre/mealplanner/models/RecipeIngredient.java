package ba.sum.fsre.mealplanner.models;

import com.google.gson.annotations.SerializedName;

public class RecipeIngredient {

    @SerializedName("id")
    private String id;

    @SerializedName("recipe_id")
    private String recipeId;

    @SerializedName("ingredient_id")
    private String ingredientId;

    private double quantity;

    @SerializedName("unit_id")
    private int unitId;

    private String note;

    public RecipeIngredient() {
    }

    public RecipeIngredient(
            String recipeId,
            String ingredientId,
            double quantity,
            int unitId,
            String note
    ) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public double getQuantity() {
        return quantity;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getNote() {
        return note;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
