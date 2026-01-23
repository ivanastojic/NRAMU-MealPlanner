package com.example.mealplanner.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.mealplanner.R;
import com.example.mealplanner.activities.ShoppingListsActivity;
import com.example.mealplanner.activities.MyMealPlansActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID = "mealplanner_channel";
    public static final String CHANNEL_NAME = "MealPlanner Notifications";

    public static final int NOTIF_DAILY_BASE = 1000;
    public static final int NOTIF_SHOPPING_READY = 2000;

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders and notifications for MealPlanner");

            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private static boolean hasPostNotificationsPermission(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void showDailyReminder(Context ctx, String title, String text, int requestCode) {
        ensureChannel(ctx);

        if (!hasPostNotificationsPermission(ctx)) return;

        Intent open = new Intent(ctx, MyMealPlansActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                ctx,
                requestCode,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_more_vert)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat.from(ctx).notify(NOTIF_DAILY_BASE + requestCode, b.build());
    }

    public static void showShoppingReady(Context ctx) {
        ensureChannel(ctx);

        if (!hasPostNotificationsPermission(ctx)) return;

        Intent open = new Intent(ctx, ShoppingListsActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                ctx,
                777,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_more_vert)
                .setContentTitle("Shopping list is ready")
                .setContentText("Open your shopping list.")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat.from(ctx).notify(NOTIF_SHOPPING_READY, b.build());
    }
}
