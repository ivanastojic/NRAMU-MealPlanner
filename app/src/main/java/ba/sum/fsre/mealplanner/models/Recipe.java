package ba.sum.fsre.mealplanner.models;

public class Recipe {

    private String id;
    private String user_id;
    private String title;

    public Recipe() {}

    public Recipe(String title, String user_id) {
        this.title = title;
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUser_id() {
        return user_id;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}