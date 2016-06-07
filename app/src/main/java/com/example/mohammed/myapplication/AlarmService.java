package com.example.mohammed.myapplication;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by mohammed on 5/29/16.
 */
public class AlarmService extends IntentService {

    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";

    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";
    LocalBroadcastManager broadcaster;
    int notificationID = 12;
    int count;
    String zekr;
    boolean vibrate;
    SharedPreferences sharedPref;
    String soundUri;


    public AlarmService() {
        super("running");
    }



    @Override
    public void onCreate() {

        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        count = preferences.getInt("countKey", 0);
        if (count >= 5) {
            count = 0;
        } else {
            count++;
        }

        zekr = getResources().getStringArray(R.array.array_ar)[count];
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("countKey", count);
        editor.apply();
        sendResult(zekr);


    }

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if (message != null)
            intent.putExtra(COPA_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
// Do the task here
        // AlarmReceiver.completeWakefulIntent(intent);
        Log.e("AlarmService", "Service running");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        soundUri = sharedPref.getString("ringtone_pref", "android.resource://" + getPackageName() + "/" + R.raw.notification);
        vibrate = sharedPref.getBoolean("vibrate_pref", true);


        Intent notifyIntent =
                new Intent(this, MainActivity.class);
        notifyIntent.putExtra("zekrData", zekr);
// Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
// Creates the PendingIntent
        PendingIntent pIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm_clock)
                        .setContentTitle(getResources().getString(R.string.notification_title_ar))
                        .setAutoCancel(true)
                        .setContentIntent(pIntent)
                        .setVibrate((vibrate) ? new long[]{50, 100, 100} : new long[]{})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setSound(Uri.parse(soundUri))
                        .setContentText(zekr);



        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());
        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);

    }


    @Override
    public void onDestroy() {
        Log.e("AlarmService", "Service stopped");
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
