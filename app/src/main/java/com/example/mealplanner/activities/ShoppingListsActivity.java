package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.adapters.ShoppingListsAdapter;
import com.example.mealplanner.api.RetrofitClient;
import com.example.mealplanner.api.SupabaseAPI;
import com.example.mealplanner.models.ShoppingList;
import com.example.mealplanner.utils.AuthManager;
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
            Toast.makeText(this, "Nema tokena - prijavi se ponovo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = "Bearer " + token;
        api = RetrofitClient.getInstance().getApi();

        adapter.setListener(new ShoppingListsAdapter.Listener() {
            @Override
            public void onOpen(ShoppingList list) {
                Intent i = new Intent(ShoppingListsActivity.this, ShoppingListDetailsActivity.class);
                i.putExtra("list_id", list.id);
                i.putExtra("date_from", list.date_from);
                i.putExtra("date_to", list.date_to);
                startActivity(i);
            }

            @Override
            public void onDelete(ShoppingList list) {
                deleteList(list.id);
            }
        });

        fabAddShoppingList.setOnClickListener(v -> {
            startActivity(new Intent(this, GenerateShoppingListActivity.class));
        });

        loadLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLists();
    }

    private void loadLists() {
        api.getShoppingLists(auth, "date_from.desc").enqueue(new Callback<List<ShoppingList>>() {
            @Override
            public void onResponse(Call<List<ShoppingList>> call, Response<List<ShoppingList>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ShoppingListsActivity.this, "Greška: " + response.code(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ShoppingListsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteList(String listId) {
        api.deleteShoppingList(auth, "eq." + listId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ShoppingListsActivity.this, "Delete greška: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                loadLists();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ShoppingListsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}