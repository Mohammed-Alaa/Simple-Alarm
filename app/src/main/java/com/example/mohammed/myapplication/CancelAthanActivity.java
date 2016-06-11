package com.example.mohammed.myapplication;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by mohammed on 6/11/16.
 */
public class CancelAthanActivity extends Activity {

    public static final String NOTIFICATION_ID = "com.example.mohammed.myapplication.NOTIFICATION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // stop athan audio
        Intent stopAudioIntent = new Intent(CancelAthanActivity.this, AlarmService.class);
        //AlarmReceiver.completeWakefulIntent(stopAudioIntent);
        stopService(stopAudioIntent);

        // cancel notification
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(getIntent().getIntExtra(NOTIFICATION_ID, -1));

        finish(); // since finish() is called in onCreate(), onDestroy() will be called immediately
    }

    public static PendingIntent getCancelAthanIntent(int notificationId, Context context) {
        Intent intent = new Intent(context, CancelAthanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(NOTIFICATION_ID, notificationId);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}