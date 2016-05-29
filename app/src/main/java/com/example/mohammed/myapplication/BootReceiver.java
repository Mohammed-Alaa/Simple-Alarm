package com.example.mohammed.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by mohammed on 5/29/16.
 */
public class BootReceiver extends BroadcastReceiver {

    AlarmReceiver alarm = new AlarmReceiver();

    private void enableBootReciver(Context context) {

        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
            // context.startService(new Intent(context, TaskButlerService.class)); //start TaskButlerService
            enableBootReciver(context);
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
                alarm.setAlarm(context);
            }
        }





}
