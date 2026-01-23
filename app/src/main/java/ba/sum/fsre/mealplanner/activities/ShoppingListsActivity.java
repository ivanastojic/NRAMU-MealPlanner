package ba.sum.fsre.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.adapters.ShoppingListsAdapter;
import ba.sum.fsre.mealplanner.api.RetrofitClient;
import ba.sum.fsre.mealplanner.api.SupabaseAPI;
import ba.sum.fsre.mealplanner.models.ShoppingList;
import ba.sum.fsre.mealplanner.utils.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListsActivity extends AppCompatActivity {

    private RecyclerView rvShoppingLists;
    private TextView tvEmptyShoppingLists;
    private FloatingActionButton fabAddShoppingList;

    private ShoppingListsAdapter adapter;

    private AuthManager authManager;
    private SupabaseAPI api;
    private String auth;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_lists);

        rvShoppingLists = findViewById(R.id.rvShoppingLists);
        tvEmptyShoppingLists = findViewById(R.id.tvEmptyShoppingLists);
        fabAddShoppingList = findViewById(R.id.fabAddShoppingList);

        adapter = new ShoppingListsAdapter();
        rvShoppingLists.setLayoutManager(new LinearLayoutManager(this));
        rvShoppingLists.setAdapter(adapter);

        authManager = new AuthManager(this);
        String token = authManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found – please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();

        setupBottomNav();

        adapter.setListener(new ShoppingListsAdapter.Listener() {
            @Override
            public void onOpen(ShoppingList list) {
                Intent i = new Intent(ShoppingListsActivity.this, ShoppingListDetailsActivity.class);
                i.putExtra("list_id", list.id);
                i.putExtra("date_from", list.date_from);
                i.putExtra("date_to", list.date_to);
                startActivity(i);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onDelete(ShoppingList list) {
                new androidx.appcompat.app.AlertDialog.Builder(ShoppingListsActivity.this)
                        .setTitle("Delete list?")
                        .setMessage("Are you sure you want to delete this shopping list? This action cannot be undone.")
                        .setPositiveButton("Yes", (d, w) -> deleteList(list.id))
                        .setNegativeButton("No", (d, w) -> d.dismiss())
                        .show();
            }
        });

        fabAddShoppingList.setOnClickListener(v -> {
            startActivity(new Intent(this, GenerateShoppingListActivity.class));
            overridePendingTransition(0, 0);
        });

        loadLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_shopping);
        loadLists();
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_shopping);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_shopping) return true;

            Intent i = null;

            if (id == R.id.nav_home) i = new Intent(this, MainActivity.class);
            else if (id == R.id.nav_recipes) i = new Intent(this, RecipesListActivity.class);
            else if (id == R.id.nav_planner) i = new Intent(this, MyMealPlansActivity.class);
            else if (id == R.id.nav_profile) i = new Intent(this, ProfileActivity.class);

            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadLists() {
        api.getShoppingLists(auth, "date_from.desc").enqueue(new Callback<List<ShoppingList>>() {
            @Override
            public void onResponse(Call<List<ShoppingList>> call, Response<List<ShoppingList>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ShoppingListsActivity.this,
                            "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ShoppingList> lists = response.body();
                adapter.setItems(lists);

                boolean empty = (lists == null || lists.isEmpty());
                tvEmptyShoppingLists.setVisibility(empty ? TextView.VISIBLE : TextView.GONE);
                rvShoppingLists.setVisibility(empty ? RecyclerView.GONE : RecyclerView.VISIBLE);
            }

            @Override
            public void onFailure(Call<List<ShoppingList>> call, Throwable t) {
                Toast.makeText(ShoppingListsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteList(String listId) {
        api.deleteShoppingList(auth, "eq." + listId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ShoppingListsActivity.this,
                            "Delete error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                loadLists();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ShoppingListsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
