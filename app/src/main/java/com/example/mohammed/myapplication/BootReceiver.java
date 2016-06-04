package com.example.mohammed.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by mohammed on 5/29/16.
 */
public class BootReceiver extends BroadcastReceiver {

    Calendar calendar;
AlarmReceiver alarm=new AlarmReceiver();



    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
             alarm.setAlarm(context);

    }





}
