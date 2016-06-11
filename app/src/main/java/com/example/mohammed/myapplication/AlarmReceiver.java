package com.example.mohammed.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by mohammed on 5/29/16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
    public final static String EXTRA_EVENT_ID = "com.example.mohammed.EVENT_ID";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        String message = intent.getStringExtra(MainActivity.NOTIFY_MESSAGE);
        Intent service = new Intent(context, AlarmService.class);
        context.startService(service);

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
                        .setSmallIcon(R.drawable.alarm)
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


    public void setAlarm(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int hours= preferences.getInt("hours", 0);
        int minutes= preferences.getInt("minutes", 0);


        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));


        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);



       Log.e("time after boot ", "" +hours+" "+minutes);


        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


}
