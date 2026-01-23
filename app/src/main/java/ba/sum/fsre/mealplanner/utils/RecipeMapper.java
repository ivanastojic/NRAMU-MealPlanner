package ba.sum.fsre.mealplanner.utils;

import ba.sum.fsre.mealplanner.models.Ingredient;
import ba.sum.fsre.mealplanner.models.IngredientDisplay;
import ba.sum.fsre.mealplanner.models.RecipeIngredient;
import ba.sum.fsre.mealplanner.models.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMapper {

    public static List<IngredientDisplay> mapToDisplay(
            List<RecipeIngredient> recipeIngredients,
            List<Ingredient> ingredients,
            List<Unit> units
    ) {
        Map<String, String> ingredientNameById = new HashMap<>();
        for (Ingredient i : ingredients) {
            ingredientNameById.put(i.getId(), i.getName());
        }

        Map<Integer, String> unitNameById = new HashMap<>();
        for (Unit u : units) {
            unitNameById.put(u.getId(), u.getName());
        }

        List<IngredientDisplay> out = new ArrayList<>();

        for (RecipeIngredient ri : recipeIngredients) {
            String ingName = ingredientNameById.get(ri.getIngredientId());
            if (ingName == null) ingName = "Nepoznat sastojak";

            String unitName = unitNameById.get(ri.getUnitId());
            if (unitName == null) unitName = "";

            String line = ri.getQuantity() + " " + unitName;

            String note = ri.getNote();
            if (note != null && !note.trim().isEmpty()) {
                line += " • " + note;
            }

            out.add(new IngredientDisplay(
                    ri.getId(),
                    ri.getIngredientId(),
                    ingName,
                    line
            ));
        }

        return out;
    }
}
