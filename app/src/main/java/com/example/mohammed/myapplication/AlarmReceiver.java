package com.example.mohammed.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by mohammed on 5/29/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    // The pending intent that is triggered when the alarm fires.
    public final static String EXTRA_EVENT_ID = "com.example.mohammed.EVENT_ID";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        String message = intent.getStringExtra(MainActivity.NOTIFY_MESSAGE);
        Intent service = new Intent(context, AlarmService.class);
        startWakefulService(context, service);


        // Build intent for notification content
        int notificationId = 0;
        int eventId = 0;
        Intent nextPrayerIntent = new Intent(context, MainActivity.class);
        nextPrayerIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent nextPrayerPendingIntent =
                PendingIntent.getActivity(context, 0, nextPrayerIntent, 0);

        // Use another intent to stop athan from notification button
        PendingIntent cancelAthanPendingIntent =
                CancelAthanActivity.getCancelAthanIntent(notificationId, context);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(MainActivity.TAG)
                        .setContentText(message)
                        .setContentIntent(nextPrayerPendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .setDeleteIntent(cancelAthanPendingIntent)
                        .addAction(R.drawable.delete, "وقف الآذان", cancelAthanPendingIntent);


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());

        // ask Activity to update display to next prayer. TODO: delay highlighting of next prayer
        Intent updateIntent = new Intent(MainActivity.UPDATE_MESSAGE);
        context.sendBroadcast(updateIntent);



    }



}
