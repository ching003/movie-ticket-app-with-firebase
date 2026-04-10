package com.example.firebase.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.firebase.R;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "movie_showtime_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        createChannel(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String movie = intent.getStringExtra("movie");
        String showtime = intent.getStringExtra("showtime");
        int id = intent.getIntExtra("ticketId", 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Nhac gio chieu")
                .setContentText(movie + " - " + showtime + " sap bat dau")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }

    public static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Movie reminder",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Nhac gio chieu phim");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
