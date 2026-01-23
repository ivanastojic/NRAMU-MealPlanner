package ba.sum.fsre.mealplanner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ba.sum.fsre.mealplanner.utils.NotificationHelper;
import ba.sum.fsre.mealplanner.utils.ReminderScheduler;

public class DailyReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int hour = intent.getIntExtra("hour", 9);
        int minute = intent.getIntExtra("minute", 0);
        int requestCode = intent.getIntExtra("requestCode", ReminderScheduler.REQ_BREAKFAST);
        String mealType = intent.getStringExtra("mealType");


        NotificationHelper.showDailyReminder(
                context,
                "Provjeri današnji plan obroka",
                "Podsjetnik (" + (mealType != null ? mealType : "") + ")",
                requestCode
        );

        ReminderScheduler.scheduleDaily(context, hour, minute, requestCode, mealType);
    }
}
