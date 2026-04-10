package com.example.firebase.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {
    public static void schedule(Context context, int ticketId, String movieTitle, String showDate, String showTime) {
        long triggerAt = parseMillis(showDate, showTime) - 30L * 60L * 1000L;
        if (triggerAt <= System.currentTimeMillis()) {
            return;
        }
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("movie", movieTitle);
        intent.putExtra("showtime", showDate + " " + showTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ticketId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    private static long parseMillis(String showDate, String showTime) {
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .parse(showDate + " " + showTime);
            return date == null ? 0 : date.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }
}
