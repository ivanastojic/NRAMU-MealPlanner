package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.mealplanner.R;
import com.example.mealplanner.utils.AuthManager;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);

        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // MOJI RECEPTI
        Button btnMyRecipes = findViewById(R.id.btnMyRecipes);
        btnMyRecipes.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecipesListActivity.class))
        );

        // DODAJ RECEPT
        Button btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnAddRecipe.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddRecipeActivity.class))
        );

        // PLANER OBROKA (unos plana po datumu)
        Button btnMealPlanner = findViewById(R.id.btnMealPlanner);
        btnMealPlanner.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MealPlannerActivity.class))
        );

        // MOJI PLANOVI (lista svih planova)
        Button btnMyPlans = findViewById(R.id.btnMyPlans);
        btnMyPlans.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyMealPlansActivity.class))
        );

        // ODJAVA
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            authManager.logout();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
