package ba.sum.fsre.mealplanner.models;

public class MealPlanRequest {

    public String user_id;
    public String recipe_id;
    public String plan_date;
    public String meal_type;

    public MealPlanRequest(String user_id, String recipe_id, String plan_date, String meal_type) {
        this.user_id = user_id;
        this.recipe_id = recipe_id;
        this.plan_date = plan_date;
        this.meal_type = meal_type;
    }
}
