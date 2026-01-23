package ba.sum.fsre.mealplanner.models;

public class EditableRecipeIngredient {

    public String recipeIngredientId;
    public String ingredientId;
    public String ingredientName;

    public double quantity;
    public int unitId;
    public String note;

    public boolean markedForDelete = false;

    public EditableRecipeIngredient(String recipeIngredientId,
                                    String ingredientId,
                                    String ingredientName,
                                    double quantity,
                                    int unitId,
                                    String note) {
        this.recipeIngredientId = recipeIngredientId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unitId = unitId;
        this.note = note;
    }
}
