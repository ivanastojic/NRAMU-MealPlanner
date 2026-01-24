package ba.sum.fsre.mealplanner.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import ba.sum.fsre.mealplanner.receivers.DailyReminderReceiver;

public class ReminderScheduler {

    public static final int REQ_BREAKFAST = 900;
    public static final int REQ_LUNCH = 1300;
    public static final int REQ_DINNER = 1800;

    public static void scheduleAllDailyReminders(Context ctx) {
        scheduleDaily(ctx, 9, 0, REQ_BREAKFAST, "Breakfast");
        scheduleDaily(ctx, 13, 0, REQ_LUNCH, "Lunch");
        scheduleDaily(ctx, 18, 0, REQ_DINNER, "Dinner");
    }

    public static void scheduleDaily(Context ctx, int hour, int minute, int requestCode, String mealType) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent i = new Intent(ctx, DailyReminderReceiver.class);
        i.setAction("ba.sum.fsre.mealplanner.DAILY_REMINDER_" + requestCode);
        i.putExtra("hour", hour);
        i.putExtra("minute", minute);
        i.putExtra("requestCode", requestCode);
        i.putExtra("mealType", mealType);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (c.getTimeInMillis() <= System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        long triggerAt = c.getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException se) {

            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } catch (Exception e) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}