package com.example.mealplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.mealplanner.R;
import com.example.mealplanner.utils.AuthManager;
import com.example.mealplanner.utils.NotificationHelper;
import com.example.mealplanner.utils.ReminderScheduler;


public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;

    private static final int REQ_POST_NOTIF = 5001;

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

        // 1) notification channel (Android 8+)
        NotificationHelper.ensureChannel(this);

        // 2) permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIF
                );
            }
        }

        // 3) schedule daily reminders
        ReminderScheduler.scheduleAllDailyReminders(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnMyRecipes = findViewById(R.id.btnMyRecipes);
        btnMyRecipes.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecipesListActivity.class))
        );

        Button btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnAddRecipe.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddRecipeActivity.class))
        );

        Button btnMealPlanner = findViewById(R.id.btnMealPlanner);
        btnMealPlanner.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MealPlannerActivity.class))
        );

        Button btnMyPlans = findViewById(R.id.btnMyPlans);
        btnMyPlans.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyMealPlansActivity.class))
        );

        Button btnShoppingLists = findViewById(R.id.btnShoppingLists);
        btnShoppingLists.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ShoppingListsActivity.class))
        );

        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            authManager.logout();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIF) {
            // kad user odobri alarms su zakazani
        }
    }

}
