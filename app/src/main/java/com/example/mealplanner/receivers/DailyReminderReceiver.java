package com.example.mealplanner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.mealplanner.utils.NotificationHelper;
import com.example.mealplanner.utils.ReminderScheduler;

public class DailyReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int hour = intent.getIntExtra("hour", 9);
        int minute = intent.getIntExtra("minute", 0);
        int requestCode = intent.getIntExtra("requestCode", ReminderScheduler.REQ_BREAKFAST);
        String mealType = intent.getStringExtra("mealType");

        // 1) pokaži notifikaciju
        NotificationHelper.showDailyReminder(
                context,
                "Provjeri današnji plan obroka",
                "Podsjetnik (" + (mealType != null ? mealType : "") + ")",
                requestCode
        );

        // 2) odmah zakaži isti podsjetnik za sutra
        ReminderScheduler.scheduleDaily(context, hour, minute, requestCode, mealType);
    }
}
