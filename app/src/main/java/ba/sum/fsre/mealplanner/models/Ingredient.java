package ba.sum.fsre.mealplanner.models;

import com.google.gson.annotations.SerializedName;

public class Ingredient {

    private String id;

    @SerializedName("user_id")
    private String userId;

    private String name;
    private String category;

    public Ingredient() {
    }

    public Ingredient(String userId, String name, String category) {
        this.userId = userId;
        this.name = name;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
