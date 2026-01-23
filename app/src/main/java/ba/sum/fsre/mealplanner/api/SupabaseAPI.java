package ba.sum.fsre.mealplanner.api;

import ba.sum.fsre.mealplanner.models.AuthResponse;
import ba.sum.fsre.mealplanner.models.Ingredient;
import ba.sum.fsre.mealplanner.models.LoginRequest;
import ba.sum.fsre.mealplanner.models.MealPlanRow;
import ba.sum.fsre.mealplanner.models.Recipe;
import ba.sum.fsre.mealplanner.models.RecipeIngredient;
import ba.sum.fsre.mealplanner.models.RegisterRequest;
import ba.sum.fsre.mealplanner.models.ShoppingItem;
import ba.sum.fsre.mealplanner.models.ShoppingList;
import ba.sum.fsre.mealplanner.models.Unit;
import ba.sum.fsre.mealplanner.utils.Constants;
import ba.sum.fsre.mealplanner.models.Profile;
import ba.sum.fsre.mealplanner.models.MealPlan;
import ba.sum.fsre.mealplanner.models.MealPlanRequest;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.PUT;
import retrofit2.http.Url;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseAPI {

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/recipes")
    Call<List<Recipe>> updateRecipe(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Recipe recipe
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/recipes")
    Call<Void> deleteRecipe(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=*")
    Call<Object> getMyMealPlans(
            @Header("Authorization") String authToken
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/profiles?select=*")
    Call<Object> getMyProfile(
            @Header("Authorization") String authToken
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/ingredients")
    Call<List<Ingredient>> insertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation,resolution=merge-duplicates"
    })
    @POST("rest/v1/ingredients?on_conflict=user_id,name")
    Call<List<Ingredient>> upsertIngredient(
            @Header("Authorization") String authToken,
            @Body Ingredient ingredient
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/ingredients?select=*")
    Call<List<Ingredient>> getIngredients(
            @Header("Authorization") String authToken
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/units?select=*")
    Call<List<Unit>> getUnits(
            @Header("Authorization") String authToken
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Prefer: return=representation"
    })
    @POST("rest/v1/recipes")
    Call<List<Recipe>> createRecipe(
            @Header("Authorization") String authToken,
            @Body Recipe recipe
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/recipes?select=*")
    Call<List<Recipe>> getRecipesByUser(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Prefer: return=representation"
    })
    @POST("rest/v1/recipe_ingredients")
    Call<List<RecipeIngredient>> addIngredientToRecipe(
            @Header("Authorization") String authToken,
            @Body RecipeIngredient recipeIngredient
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/recipe_ingredients?select=*")
    Call<List<RecipeIngredient>> getRecipeIngredientsByRecipeId(
            @Header("Authorization") String authToken,
            @Query("recipe_id") String recipeIdFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/recipe_ingredients")
    Call<Void> deleteRecipeIngredientById(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/recipe_ingredients")
    Call<List<RecipeIngredient>> updateRecipeIngredient(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body RecipeIngredient body
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY,
            "Prefer: return=representation"
    })
    @POST("rest/v1/meal_plans")
    Call<List<MealPlan>> insertMealPlan(
            @Header("Authorization") String authToken,
            @Body MealPlanRequest body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=*")
    Call<List<MealPlan>> getMealPlansByDate(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter,
            @Query("plan_date") String dateFilter,
            @Query("order") String order
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/meal_plans")
    Call<Void> deleteMealPlan(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=id,plan_date,meal_type,recipe_id")
    Call<List<MealPlanRow>> getMyMealPlansAll(
            @Header("Authorization") String authToken,
            @Query("order") String order
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/meal_plans")
    Call<List<MealPlan>> updateMealPlan(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/shopping_lists?select=*")
    Call<List<ShoppingList>> getShoppingLists(
            @Header("Authorization") String authToken,
            @Query("order") String order
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/shopping_lists?select=*")
    Call<List<ShoppingList>> getShoppingListByWeek(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter,
            @Query("date_from") String fromFilter,
            @Query("date_to") String toFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/shopping_lists")
    Call<List<ShoppingList>> createShoppingList(
            @Header("Authorization") String authToken,
            @Body ShoppingList body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/shopping_lists")
    Call<List<ShoppingList>> updateShoppingList(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/shopping_lists")
    Call<Void> deleteShoppingList(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/shopping_items?select=*")
    Call<List<ShoppingItem>> getShoppingItemsByList(
            @Header("Authorization") String authToken,
            @Query("shopping_list_id") String listIdFilter,
            @Query("order") String order
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/shopping_items")
    Call<Void> deleteShoppingItemsByList(
            @Header("Authorization") String authToken,
            @Query("shopping_list_id") String listIdFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/shopping_items")
    Call<List<ShoppingItem>> insertShoppingItems(
            @Header("Authorization") String authToken,
            @Body List<ShoppingItem> items
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/shopping_items")
    Call<List<ShoppingItem>> updateShoppingItem(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/meal_plans?select=id,recipe_id,plan_date,meal_type")
    Call<List<MealPlan>> getMealPlansByRange(
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter,
            @Query(value = "plan_date", encoded = true) List<String> planDateFilters,
            @Query("order") String order
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/recipe_ingredients?select=ingredient_id,unit_id,quantity,recipe_id")
    Call<List<RecipeIngredient>> getRecipeIngredientsByRecipeIds(
            @Header("Authorization") String authToken,
            @Query(value = "recipe_id", encoded = true) String recipeIdInFilter
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/shopping_items")
    Call<List<ShoppingItem>> updateShoppingItemsByList(
            @Header("Authorization") String authToken,
            @Query("shopping_list_id") String listIdFilter,
            @Body Map<String, Object> body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @GET("rest/v1/profiles?select=id,email,full_name,avatar_url")
    Call<List<Profile>> getMyProfileTyped(
            @Header("Authorization") String authToken
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/profiles")
    Call<List<Profile>> updateProfile(
            @Header("Authorization") String authToken,
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );
    @Headers({
            "apikey: " + Constants.ANON_KEY
    })
    @PUT
    Call<ResponseBody> uploadAvatar(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Header("x-upsert") String upsert,
            @Url String url,
            @Body RequestBody body
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/shopping_items")
    Call<Void> deleteShoppingItemsByUser(
            @Header("Authorization") String token,
            @Query("user_id") String userId
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/shopping_lists")
    Call<Void> deleteShoppingListsByUser(
            @Header("Authorization") String token,
            @Query("user_id") String userId
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/meal_plans")
    Call<Void> deleteMealPlansByUser(
            @Header("Authorization") String token,
            @Query("user_id") String userId
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/recipes")
    Call<Void> deleteRecipesByUser(
            @Header("Authorization") String token,
            @Query("user_id") String userId
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/ingredients")
    Call<Void> deleteIngredientsByUser(
            @Header("Authorization") String token,
            @Query("user_id") String userId
    );

    @Headers({
            "apikey: " + Constants.ANON_KEY,
            "Accept: application/json"
    })
    @DELETE("rest/v1/profiles")
    Call<Void> deleteProfile(
            @Header("Authorization") String token,
            @Query("id") String userId
    );

    @Headers({
            "Content-Type: application/json",
            "apikey: " + Constants.ANON_KEY
    })
    @POST("functions/v1/swift-responder")
    Call<ResponseBody> deleteAuthUser(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );
}
